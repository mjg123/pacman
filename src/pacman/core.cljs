(ns pacman.core
  (:require [pacman.ui :as ui]
    [pacman.board :as board]
    [pacman.util :as util]
    [pacman.keys :as keyz]
    [pacman.ghosts :as ghosts]
    [pacman.tile :as _tile]
    [goog.Timer :as timer]
    [goog.date :as date]))

(def start-tiles {
  :pacman (_tile/tile 14 26)
  :blinky (_tile/tile 14 14)
  :pinky (_tile/tile 12 14)
  :inky (_tile/tile 13 14)
  :clyde (_tile/tile 15 14)
  })

(def level-info {:pacman-speed 0.8 :ghost-speed 0.75 :ghost-tunnel-speed 0.4})

(def points {:pellet 10 :energy 50 :no-food 0})

(def deltas {:west [-1 0] :east [1 0] :north [0 -1] :south [0 1] :none [0 0]})

(def pacman-start {:pos (_tile/left (start-tiles :pacman))
                   :tile (start-tiles :pacman)
                   :face :west
                   :move-dir :west})

(def ghosts {:blinky {:pos (_tile/left (start-tiles :blinky))
                      :tile (start-tiles :blinky)
                      :target-tile (_tile/tile 25 0)
                      :home (_tile/tile 25 0)
                      :face :west ; TODO - choose starting face better?
                      :next-turn :none}
             :pinky {:pos (_tile/left (start-tiles :pinky))
                     :tile (start-tiles :pinky)
                     :target-tile (_tile/tile 2 0)
                     :home (_tile/tile 2 0)
                     :face :west ; TODO - choose starting face better?
                     :next-turn :none}
             :inky {:pos (_tile/left (start-tiles :inky))
                    :tile (start-tiles :inky)
                    :target-tile (_tile/tile 27 35)
                    :home (_tile/tile 27 35)
                    :face :west ; TODO - choose starting face better?
                    :next-turn :none}
             :clyde {:pos (_tile/left (start-tiles :clyde))
                     :tile (start-tiles :clyde)
                     :target-tile (_tile/tile 0 35)
                     :home (_tile/tile 0 35)
                     :face :west ; TODO - choose starting face better?
                     :next-turn :none}})

(def opposite-dir {:east :west
                   :west :east
                   :north :south
                   :south :north})

(defn get-new-move-dir [x y kp old-dir board]

  ;; Reverse direction - don't need to be on a tile centre to do this.
  (if (= old-dir (opposite-dir kp)) kp

    (if (= kp :stop) :none

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
          old-dir)))))

(defn get-pacman-speed [pman]
  (if (pman :frozen)
    0
    (level-info :pacman-speed)))

(defn tick-pacman [state kp]
  (let [{old :pacman board :board tick :tick} state
        [x y] (old :pos)
        new-move-dir (get-new-move-dir x y kp (old :move-dir) board)
        new-face (if (= :none new-move-dir) (old :face) new-move-dir)
        [dx dy] (map #(* (get-pacman-speed old) %) (deltas new-move-dir))
        [nx ny] [(mod (+ 224 x dx) 224) (+ y dy)]
        new-tile (_tile/tile-at nx ny)]

    (ui/put-pacman! [nx ny] new-face tick)

    (let [[new-board score-diff frozen]
          (if (and (not= (get-in board [new-tile :food]) :no-food) (not= (old :tile) new-tile))
            (do
              (ui/eat-at! new-tile)
              [(assoc-in board [new-tile :food] :no-food) (points (get-in board [new-tile :food])) true])
            [board 0 false])]

      (assoc state
        :pacman (assoc old
                    :face new-face
                    :move-dir new-move-dir
                    :pos [nx ny]
                    :tile new-tile
                    :frozen frozen)
        :score (+ (state :score) score-diff)
        :board new-board))))

(defn ghost-turn [ghost]
  (let [[x y] (ghost :pos)]
    (if (_tile/tile-center? x y)
      (ghost :next-turn)
      (ghost :face))))

(defn in-tunnel? [[x y]]
  (and (= y 17)
    (or (< x 6) (> x 21))))

(defn ghost-speed [tile]
  (if (in-tunnel? tile)
    (level-info :ghost-tunnel-speed)
    (level-info :ghost-speed)))

(defn tick-ghost [name ghost]

  (let [[x y] (ghost :pos)
        new-face (ghost-turn ghost)
        [dx dy] (map #(* (ghost-speed (ghost :tile)) %) (deltas new-face))
        [nx ny] [(mod (+ 224 x dx) 224) (+ y dy)]
        new-tile (_tile/tile-at nx ny)]

    (ui/put-ghost! name [nx ny] new-face (ghost :target-tile))

    (let [next-turn
          (if (not= (ghost :tile) new-tile)
            (let [exits (board/exits new-tile)
                  valid-exits (remove #(= (opposite-dir (ghost :face)) %) exits)]

              (if (= 1 (count valid-exits))
                (first valid-exits)
                (ghosts/choose-exit new-tile (ghost :target-tile) valid-exits)))

            (ghost :next-turn))]

      (assoc ghost
        :pos [nx ny]
        :tile new-tile
        :face new-face
        :next-turn next-turn))))

(defn tick-ghosts [old]
  (assoc old
    :blinky (tick-ghost :blinky (old :blinky))
    :pinky (tick-ghost :pinky (old :pinky))
    :inky (tick-ghost :inky (old :inky))
    :clyde (tick-ghost :clyde (old :clyde))))

(defn is-scatter? [tick]
  (= 3 (mod (Math/floor (/ tick 250)) 4))) ; scatter for final 250 of every 1000

(defn tick-ghost-targets [{g :ghosts p :pacman t :tick}]
  (let [scatter (is-scatter? t)]
    (if scatter
      (do
        (-> g
          (assoc-in [:blinky :target-tile] (get-in g [:blinky :home]))
          (assoc-in [:pinky :target-tile] (get-in g [:pinky :home]))
          (assoc-in [:inky :target-tile] (get-in g [:inky :home]))
          (assoc-in [:clyde :target-tile] (get-in g [:clyde :home]))))

      (do
        (-> g
          (assoc-in [:blinky :target-tile] (p :tile))
          (assoc-in [:pinky :target-tile] (ghosts/pinky-target p))
          (assoc-in [:inky :target-tile] (ghosts/inky-target p (g :blinky)))
          (assoc-in [:clyde :target-tile] (ghosts/clyde-target p (g :clyde))))))))

(defn next-state [state kp]
  (let [targetted-ghosts (tick-ghost-targets state)
        updated-state (tick-pacman state kp)]
    (assoc updated-state
      :tick (inc (state :tick))
      :ghosts (tick-ghosts targetted-ghosts))))

(defn current-time []
  (. (date/DateTime.) (getTime)))

(defn gameloop [state loopstart]

  (let [next (next-state state (keyz/kp))
        now (current-time)
        time-taken (- now loopstart)
        sleep (- 33 time-taken)
        real-sleep (if (> 0 sleep) 0 sleep)]

    (if (not= sleep real-sleep)
      (util/log "TOO SLOW")
      (util/log ""))

    (if (not= (state :score) (next :score))
      (ui/update-score! (next :score)))

    (timer/callOnce #(gameloop next now) real-sleep)))

(let [board (board/load)]
  (util/log "starting up")
  (ui/initialize board pacman-start ghosts)
  (keyz/listen)
  (gameloop {:pacman pacman-start
             :ghosts ghosts
             :board board
             :tick 0
             :score 0
             :frozen false} (current-time)))


