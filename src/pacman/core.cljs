(ns pacman.core
  (:require [pacman.ui :as ui]
    [pacman.board :as board]
    [pacman.util :as util]
    [pacman.keys :as keyz]
    [pacman.ghosts :as ghosts]
    [pacman.tile :as _tile]
    [pacman.levels :as levels]
    [goog.Timer :as timer]
    [goog.date :as date]))

(def points {:pellet 10 :energy 50 :no-food 0})

(def deltas {:west [-1 0] :east [1 0] :north [0 -1] :south [0 1] :none [0 0]})

(def pacman-start {:pos (_tile/left (_tile/tile 14 26))
                   :tile (_tile/tile 14 26)
                   :face :west
                   :move-dir :west})

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

(defn get-pacman-speed [state]
  (if (get-in state [:pacman :frozen])
    0
    (get-in state [:level-info :pacman-speed])))

(defn tick-pacman [state kp]
  (let [{old :pacman board :board tick :tick} state
        [x y] (old :pos)
        new-move-dir (get-new-move-dir x y kp (old :move-dir) board)
        new-face (if (= :none new-move-dir) (old :face) new-move-dir)
        [dx dy] (map #(* (get-pacman-speed state) %) (deltas new-move-dir))
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

(defn ghost-speed [tile level-info]
  (if (in-tunnel? tile)
    (level-info :ghost-tunnel-speed)
    (level-info :ghost-speed)))

(defn tick-ghost [name ghost level-info]

  (let [[x y] (ghost :pos)
        new-face (ghost-turn ghost)
        [dx dy] (map #(* (ghost-speed (ghost :tile) level-info) %) (deltas new-face))
        [nx ny] [(mod (+ 224 x dx) 224) (+ y dy)]
        new-tile (_tile/tile-at nx ny)]

    (ui/put-ghost! name [nx ny] new-face (ghost :target-tile))

    (let [next-turn
          (if (not= (ghost :tile) new-tile)
            (let [exits (board/prop new-tile :ghost-exits)
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

(defn tick-ghosts [state]
  (let [ghosts (state :ghosts)]
    (assoc ghosts
      :blinky (tick-ghost :blinky (ghosts :blinky) (state :level-info))
      :pinky (tick-ghost :pinky (ghosts :pinky) (state :level-info))
      :inky (tick-ghost :inky (ghosts :inky) (state :level-info))
      :clyde (tick-ghost :clyde (ghosts :clyde) (state :level-info)))))

(defn tick-ghost-targets [{g :ghosts p :pacman t :tick mode :ghost-mode}]

  (if (= :scatter mode)
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
        (assoc-in [:clyde :target-tile] (ghosts/clyde-target p (g :clyde)))))))

(defn new-ghost-mode [{tick :tick {times :ghost-mode-times} :level-info old-mode :ghost-mode}]
  (get times tick old-mode))

(defn next-state [old-state kp]

  (let [updated-state (-> old-state
        (assoc :ghost-mode (new-ghost-mode old-state))
    (assoc :ghosts (tick-ghost-targets old-state))
    (tick-pacman kp)
    (assoc :tick (inc (old-state :tick))))]

    (assoc updated-state
      :ghosts (tick-ghosts updated-state))))

(defn current-time []
  (. (date/DateTime.) (getTime)))

(defn gameloop [state loopstart]
  (let [next (next-state state (keyz/kp))
        now (current-time)
        time-taken (- now loopstart)
        sleep (- 33 time-taken)
        real-sleep (if (> 0 sleep) 0 sleep)]

    (if (not= sleep real-sleep)
      (util/log (str "TOO SLOW @ " (state :tick))))

    (if (not= (state :score) (next :score))
      (ui/update-score! (next :score)))

    (timer/callOnce #(gameloop next now) real-sleep)))

(let [board (board/load)]
  (ui/initialize board pacman-start (ghosts/init))
  (keyz/listen)
  (gameloop {:pacman pacman-start
             :ghosts (ghosts/init)
             :board board
             :tick 0
             :score 0
             :frozen false ;TODO this should be a count, in :pacman
             :level-info (levels/level-info 1)}
            (current-time)))

