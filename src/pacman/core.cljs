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
(def freeze-times {:pellet 1 :energy 3 :no-food 0})

(def deltas {:west [-1 0] :east [1 0] :north [0 -1] :south [0 1] :none [0 0]})

(def pacman-start {:pos (_tile/left (_tile/tile 14 26))
                   :tile (_tile/tile 14 26)
                   :face :west
                   :move-dir :west
                   :food-count 0
                   :freeze 0
                   :dead false
                   :ghost-eat-multiplier 0})

(def opposite-dir {:east :west
                   :west :east
                   :north :south
                   :south :north
                   :none :none})

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
  (if (not= 0 (get-in state [:pacman :freeze]))
    0
    (get-in state [:level-info :pacman-speed])))

(defn dec-if-gt-zero [x]
  (if (< 0 x) (dec x) 0))

(defn frightened? [ghost]
  (< 0 (ghost :fright-ticks)))

(defn pman-killed-by-ghost? [state]
  (or
    (and (not (frightened? (get-in state [:ghosts :blinky]))) (= (get-in state [:pacman :tile]) (get-in state [:ghosts :blinky :tile])))
    (and (not (frightened? (get-in state [:ghosts :pinky]))) (= (get-in state [:pacman :tile]) (get-in state [:ghosts :pinky :tile])))
    (and (not (frightened? (get-in state [:ghosts :inky]))) (= (get-in state [:pacman :tile]) (get-in state [:ghosts :inky :tile])))
    (and (not (frightened? (get-in state [:ghosts :clyde]))) (= (get-in state [:pacman :tile]) (get-in state [:ghosts :clyde :tile])))))

(defn get-fright-ticks-for [ghost state]
  (let [ghost (get-in state [:ghosts ghost])
        fright-time (get-in state [:level-info :ghost-fright-time])]
    (cond
      (ghost :in-da-house) 0
      :else fright-time)))

(defn tick-pacman [state kp]
  (let [{old :pacman board :board tick :tick} state
        [x y] (old :pos)
        new-move-dir (get-new-move-dir x y kp (old :move-dir) board)
        new-face (if (= :none new-move-dir) (old :face) new-move-dir)
        [dx dy] (map #(* (get-pacman-speed state) %) (deltas new-move-dir))
        [nx ny] [(mod (+ 224 x dx) 224) (+ y dy)]
        new-tile (_tile/tile-at nx ny)]

    (ui/put-pacman! [nx ny] new-face tick)

    (let [[new-board score-diff freeze food-count new-ghosts ghost-eat-multiplier]
          (if (and (not= (get-in board [new-tile :food]) :no-food) (not= (old :tile) new-tile)) ; ie we moved into a new tile, which has food in it.
            (do
              (ui/eat-at! new-tile)

              [(assoc-in board [new-tile :food] :no-food)
               (points (get-in board [new-tile :food]))
               (freeze-times (get-in board [new-tile :food]))
               (inc (old :food-count))
               (if (= :energy (get-in board [new-tile :food]))
                 (-> (state :ghosts)
                   (assoc-in [:blinky :fright-ticks] (get-fright-ticks-for :blinky state))
                   (assoc-in [:pinky :fright-ticks] (get-fright-ticks-for :pinky state))
                   (assoc-in [:inky :fright-ticks] (get-fright-ticks-for :inky state))
                   (assoc-in [:clyde :fright-ticks] (get-fright-ticks-for :clyde state))
                   (assoc-in [:blinky :face] (opposite-dir (get-in state [:ghosts :blinky :face])))
                   (assoc-in [:pinky :face] (opposite-dir (get-in state [:ghosts :pinky :face])))
                   (assoc-in [:inky :face] (opposite-dir (get-in state [:ghosts :inky :face])))
                   (assoc-in [:clyde :face] (opposite-dir (get-in state [:ghosts :clyde :face]))))
                 (state :ghosts))
               200
               ])
            [board 0 (dec-if-gt-zero (old :freeze)) (old :food-count) (state :ghosts) 0])]

      (let [is-dead (pman-killed-by-ghost? state)]
        (assoc state
          :pacman (assoc old
            :face new-face
            :move-dir new-move-dir
            :pos [nx ny]
            :tile new-tile
            :food-count food-count
            :freeze freeze
            :ghost-eat-multiplier ghost-eat-multiplier
            :dead is-dead)
          :ghosts new-ghosts
          :score (+ (state :score) score-diff)
          :board new-board)))))

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

(def i Math/floor)

(defn ghost-leaving-da-house-tick [name ghost level-info]
  (let [[x y] (ghost :pos)]
    (cond
      (> (i x) 112)
      (assoc ghost
        :pos [(- x (level-info :ghost-house-speed)) y]
        :face :west)

      (< (i x) 112)
      (assoc ghost
        :pos [(+ x (level-info :ghost-house-speed)) y]
        :face :east)

      (= (i y) 116)
      (let [[tx ty] (_tile/middle (ghost :target-tile))]
        (assoc ghost
          :face (if (> tx x) :west :east)
          :in-da-house false))

      :else
      (assoc ghost
        :pos [x (- y (level-info :ghost-house-speed))]
        :face :north))))

(defn ghost-in-da-house-tick [name ghost level-info food-count]

  (if (>= food-count (get-in level-info [:ghost-house-dotcounts name]))
    (let [new-ghost (ghost-leaving-da-house-tick name ghost level-info)]
      (ui/put-ghost! name (new-ghost :pos) (new-ghost :face) (new-ghost :target-tile) :normal)
      new-ghost)

    (let [[x y] (ghost :pos)
          new-face (if (= 0 (mod (i y) 8)) (opposite-dir (ghost :face)) (ghost :face))
          [dx dy] (map #(* (level-info :ghost-house-speed) %) (deltas new-face))
          [nx ny] [(+ x dx) (+ y dy)]]

      (ui/put-ghost! name [nx ny] new-face (ghost :target-tile) :normal)

      (assoc ghost :face new-face :pos [nx ny]))))

(defn get-ghost-fright-mode [ticks]
  (cond
    (< ticks 10) :frightened
    (< ticks 20) :recovering
    (< ticks 30) :frightened
    (< ticks 40) :recovering
    (< ticks 50) :frightened
    (< ticks 60) :recovering
    :else :frightened))

(defn random-turn [ghost]
  (let [[x y] (ghost :pos)]
    (if (_tile/tile-center? x y)
      (rand-nth (remove #(= % (opposite-dir (ghost :face))) (board/prop (ghost :tile) :ghost-exits)))
      (ghost :face))))

(defn frightened-ghost-tick [name ghost level-info pacman-tile]
  (let [[x y] (ghost :pos)
        new-face (random-turn ghost)
        [dx dy] (map #(* (level-info :ghost-fright-speed) %) (deltas new-face))
        [nx ny] [(mod (+ 224 x dx) 224) (+ y dy)]
        new-tile (_tile/tile-at nx ny)
        dead (= (ghost :tile) pacman-tile)]

    (ui/put-ghost! name (ghost :pos) :none (ghost :target-tile) (get-ghost-fright-mode (ghost :fright-ticks)))

    (assoc ghost
      :pos [nx ny]
      :tile new-tile
      :face new-face
      :next-turn (opposite-dir new-face)
      :dead dead
      :fright-ticks (dec (ghost :fright-ticks)))))

(def dead-ghost-target-tile [13 14])

(defn ghost-dead-tick [name ghost]

  (if (= (_tile/right dead-ghost-target-tile) (map i (ghost :pos)))
    (assoc ghost
      :fright-ticks 0
      :dead false)

    (let [[x y] (ghost :pos)
          exits (board/prop (ghost :tile) :exits)
          valid-exits (remove #(= (opposite-dir (ghost :face)) %) exits)
          new-face (if (_tile/tile-center? x y) (ghosts/choose-exit (ghost :tile) dead-ghost-target-tile valid-exits) (ghost :face))
          [dx dy] (deltas new-face)
          [nx ny] [(mod (+ 224 x dx) 224) (+ y dy)]]
      (ui/put-ghost! name [nx ny] new-face (ghost :target-tile) :dead)
      (assoc ghost :pos [nx ny] :tile (_tile/tile-at nx ny) :face new-face))))

(defn tick-ghost [name ghost {level-info :level-info mode :ghost-mode {food-count :food-count pacman-tile :tile} :pacman} old-mode]
  (cond
    (ghost :dead) (ghost-dead-tick name ghost)
    (ghost :in-da-house) (ghost-in-da-house-tick name ghost level-info food-count)
    (< 0 (ghost :fright-ticks)) (frightened-ghost-tick name ghost level-info pacman-tile)

    :else
    (let [turnaround (not= mode old-mode)
          [x y] (ghost :pos)
          new-face (if turnaround (opposite-dir (ghost :face)) (ghost-turn ghost))
          [dx dy] (map #(* (ghost-speed (ghost :tile) level-info) %) (deltas new-face))
          [nx ny] [(mod (+ 224 x dx) 224) (+ y dy)]
          new-tile (_tile/tile-at nx ny)]

      (ui/put-ghost! name [nx ny] new-face (ghost :target-tile) :normal)

      (let [next-turn
            (if (or turnaround (not= (ghost :tile) new-tile))
              (let [exits (board/prop new-tile :ghost-exits)
                    valid-exits (remove #(= (opposite-dir new-face) %) exits)]

                (if (= 1 (count valid-exits))
                  (first valid-exits)
                  (ghosts/choose-exit new-tile (ghost :target-tile) valid-exits)))

              (ghost :next-turn))]

        (assoc ghost
          :pos [nx ny]
          :tile new-tile
          :face new-face
          :next-turn next-turn)))))

(defn tick-ghosts [old-mode state]
  (let [ghosts (state :ghosts)]
    (assoc ghosts
      :blinky (tick-ghost :blinky (ghosts :blinky) state old-mode)
      :pinky (tick-ghost :pinky (ghosts :pinky) state old-mode)
      :inky (tick-ghost :inky (ghosts :inky) state old-mode)
      :clyde (tick-ghost :clyde (ghosts :clyde) state old-mode))))

(defn tick-ghost-targets [{g :ghosts p :pacman t :tick mode :ghost-mode}]
  (cond
    (= :scatter mode)
    (-> g
      (assoc-in [:blinky :target-tile] (get-in g [:blinky :home]))
      (assoc-in [:pinky :target-tile] (get-in g [:pinky :home]))
      (assoc-in [:inky :target-tile] (get-in g [:inky :home]))
      (assoc-in [:clyde :target-tile] (get-in g [:clyde :home])))

    (= :chase mode)
    (-> g
      (assoc-in [:blinky :target-tile] (p :tile))
      (assoc-in [:pinky :target-tile] (ghosts/pinky-target p))
      (assoc-in [:inky :target-tile] (ghosts/inky-target p (g :blinky)))
      (assoc-in [:clyde :target-tile] (ghosts/clyde-target p (g :clyde))))))

(defn new-ghost-mode [{tick :ghost-pattern-tick {times :ghost-mode-times} :level-info old-mode :ghost-mode}]
  (get times tick old-mode))

(defn ghost-pattern-tick [state]
  (if (= 0 (reduce + (map #(get-in state [:ghosts % :fright-ticks]) [:blinky :pinky :inky :clyde]))) ; ie if all ghosts are not frightened
    (assoc state :ghost-pattern-tick (inc (state :ghost-pattern-tick)))
    state))

(defn next-state [old-state kp]

  ; TODO - it would nice to rewrite this using only "->"
  (let [new-state (assoc old-state :ghost-mode (new-ghost-mode old-state))
        new-state (assoc new-state :ghosts (tick-ghost-targets old-state))

        new-state (tick-pacman new-state kp)
        new-state (assoc new-state :ghosts (tick-ghosts (old-state :ghost-mode) new-state)) ; ie avoid refering to new-state here.

        new-state (assoc new-state :tick (inc (old-state :tick)))
        new-state (ghost-pattern-tick new-state)]
    new-state))

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

    (if (get-in state [:pacman :dead])
      (timer/callOnce #(wait-for-start (next :score) (next :level-no) (next :board)))

      (if (= 244 (get-in next [:pacman :food-count]))
        (timer/callOnce #(reset-and-restart (next :score) (inc (next :level-no))))
        (timer/callOnce #(gameloop next now) real-sleep)))))

(defn reset-and-restart [score level]
  (ui/reset-edibles!)
  (wait-for-start score level (board/get-default)))

(defn wait-for-start [score level board]

  (ui/show-ready-message! level)

  (if (= (keyz/kp) :start-game)
    (do
      (keyz/consume!)
      (ui/clear-ready-message!)
      (gameloop {:pacman pacman-start
                 :ghosts (ghosts/init)
                 :board board
                 :tick 0
                 :ghost-pattern-tick 0
                 :score score
                 :ghost-mode :scatter
                 :level-no level
                 :level-info (levels/level-info level)}
        (current-time)))

    (timer/callOnce #(wait-for-start score level board) 50)))

(do
  (board/load)
  (ui/initialize (board/get-default) pacman-start (ghosts/init))
  (keyz/listen)
  (wait-for-start 0 1 (board/get-default)))

