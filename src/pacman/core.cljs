(ns pacman.core
  (:require [pacman.ui :as ui]
    [pacman.board :as board]
    [pacman.util :as util]
    [pacman.keys :as keyz]
    [pacman.tile :as _tile]
    [goog.Timer :as timer]))

(def deltas {:west [-1 0] :east [1 0] :north [0 -1] :south [0 1] :none [0 0]})
(def pacman-start {:pos (_tile/left (_tile/tile 14 26))
                   :tile (_tile/tile 14 26) 
                   :face :west
                   :move-dir :west})

(defn tile-open? [board [x y]] ;TODO memoize
  (contains? (board [x y]) :open))

(defn get-new-move-dir [x y kp old-dir board]
  (let [[tx ty] (_tile/tile-at x y)]
    (if (_tile/tile-center? x y) ; TODO - allow fast cornering
      (cond
        ;; Reverse direction
        (and (= :west kp) (= :east old-dir)) :west
        (and (= :east kp) (= :west old-dir)) :east
        (and (= :north kp) (= :south old-dir)) :north
        (and (= :south kp) (= :north old-dir)) :south

        ;; Change direction
        (and (= :north kp) (tile-open? board [tx (- ty 1)])) (do (keyz/consume!) kp)
        (and (= :south kp) (tile-open? board [tx (+ ty 1)])) (do (keyz/consume!) kp)
        (and (= :east kp) (tile-open? board [(+ tx 1) ty])) (do (keyz/consume!) kp)
        (and (= :west kp) (tile-open? board [(- tx 1) ty])) (do (keyz/consume!) kp)

        ;; Keep going (if we can)
        (and (= :north old-dir) (tile-open? board [tx (- ty 1)])) old-dir
        (and (= :south old-dir) (tile-open? board [tx (+ ty 1)])) old-dir
        (and (= :east old-dir) (tile-open? board [(+ tx 1) ty])) old-dir
        (and (= :west old-dir) (tile-open? board [(- tx 1) ty])) old-dir

        :else :none)
      old-dir)))

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


(defn next-state [state kp]
  (assoc state
    :tick (inc (state :tick))
    :pacman (update-pacman (state :pacman) kp (state :board))))

(defn gameloop [state]
  (timer/callOnce #(gameloop (next-state state (keyz/kp))) 17)) ;; NB we're getting the mutable keypress state here!

(let [board (board/load)]
  (util/log "starting up")
  (ui/initialize board pacman-start)
  (keyz/listen)
  (gameloop {:pacman pacman-start :board board :tick 0}))


