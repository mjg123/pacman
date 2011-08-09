(ns pacman.core
  (:require [pacman.ui :as ui]
    [pacman.board :as board]
    [pacman.util :as util]
    [pacman.keys :as keyz]
    [pacman.ghosts :as ghosts]
    [pacman.tile :as _tile]
    [goog.Timer :as timer]))

(def start-tiles {
  :pacman (_tile/tile 14 26)
  :blinky (_tile/tile 14 14)
  })

(def deltas {:west [-1 0] :east [1 0] :north [0 -1] :south [0 1] :none [0 0]})

(def pacman-start {:pos (_tile/left (start-tiles :pacman))
                   :tile (start-tiles :pacman)
                   :face :west
                   :move-dir :west})

(def ghosts {:blinky {:pos (_tile/left (start-tiles :blinky))
                      :tile (start-tiles :blinky)
                      :target-tile (_tile/tile 27 0)
                      :home (_tile/tile 27 0)
                      :face :west ; TODO - choose starting face better
                      :next-turn :none}})

(def opposite-dir {:east :west
                   :west :east
                   :north :south
                   :south :north})

(defn get-new-move-dir [x y kp old-dir board]

  ;; Reverse direction - don't need to be on a tile centre to do this.
  (if (= old-dir (opposite-dir kp)) kp

    (let [[tx ty] (_tile/tile-at x y)]
      (if (_tile/tile-center? x y) ; TODO - allow fast cornering
        (cond

          ;; Change direction
          (and (= :north kp) (board/tile-open? [tx (- ty 1)])) (do (keyz/consume!) kp)
          (and (= :south kp) (board/tile-open? [tx (+ ty 1)])) (do (keyz/consume!) kp)
          (and (= :east kp) (board/tile-open? [(+ tx 1) ty])) (do (keyz/consume!) kp)
          (and (= :west kp) (board/tile-open? [(- tx 1) ty])) (do (keyz/consume!) kp)

          ;; Keep going (if we can)
          (and (= :north old-dir) (board/tile-open? [tx (- ty 1)])) old-dir
          (and (= :south old-dir) (board/tile-open? [tx (+ ty 1)])) old-dir
          (and (= :east old-dir) (board/tile-open? [(+ tx 1) ty])) old-dir
          (and (= :west old-dir) (board/tile-open? [(- tx 1) ty])) old-dir

          :else :none)
        old-dir))))

(defn update-pacman [old kp board]
  (let [[x y] (old :pos)
        new-move-dir (get-new-move-dir x y kp (old :move-dir) board)
        new-face (if (= :none new-move-dir) (old :face) new-move-dir)
        [dx dy] (deltas new-move-dir)
        [nx ny] [(mod (+ 224 x dx) 224) (+ y dy)]
        new-tile (_tile/tile-at nx ny)]

    (ui/put-pacman! [nx ny] new-face)

    (if (not= (old :tile) new-tile)
      (do
        (ui/eat-at! new-tile)))

    (assoc old
      :face new-face
      :move-dir new-move-dir
      :pos [nx ny]
      :tile new-tile)))

(defn ghost-turn [ghost]
  (let [[x y] (ghost :pos)]
    (if (_tile/tile-center? x y)
      (ghost :next-turn)
      (ghost :face))))

(defn update-ghost [name ghost]

  (let [[x y] (ghost :pos)
        new-face (ghost-turn ghost)
        [dx dy] (deltas new-face)
        [nx ny] [(+ x dx) (+ y dy)]
        new-tile (_tile/tile-at nx ny)]

    (ui/put-ghost! name [nx ny] nil)

    (let [next-turn
          (if (not= (ghost :tile) new-tile)
            (let [exits (board/exits new-tile)
                  valid-exits (remove #(= (opposite-dir (ghost :face)) %) exits)]
              (ghosts/choose-exit new-tile (ghost :target-tile) valid-exits))
            (ghost :next-turn))]

      (assoc ghost
        :pos [nx ny]
        :tile new-tile
        :face new-face
        :next-turn next-turn))))

(defn update-ghosts [old]
  (assoc old
    :blinky (update-ghost :blinky (old :blinky))))

(defn is-scatter? [tick]
  (even? (Math/floor (/ tick 1000))))

(defn update-ghost-targets [g p t]
  (let [scatter (is-scatter? t)]
    (util/log (pr-str [t "is-scatter?" scatter]))
    (if scatter
      (assoc-in g [:blinky :target-tile] (get-in g [:blinky :home]))
      (assoc-in g [:blinky :target-tile] (p :tile)))))

(defn next-state [state kp]
  (let [targetted-ghosts (update-ghost-targets (state :ghosts) (state :pacman) (state :tick))] ; TODO - destructre map
    (assoc state
      :tick (inc (state :tick))
      :ghosts (update-ghosts targetted-ghosts)
      :pacman (update-pacman (state :pacman) kp (state :board)))))

(defn gameloop [state]
  (timer/callOnce #(gameloop (next-state state (keyz/kp))) 17)) ;; NB we're getting the mutable keypress state here!

(let [board (board/load)]
  (util/log "starting up")
  (ui/initialize board pacman-start ghosts)
  (keyz/listen)
  (gameloop {:pacman pacman-start :ghosts ghosts :board board :tick 0}))


