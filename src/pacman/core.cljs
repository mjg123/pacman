(ns pacman.core
  (:require [pacman.ui :as ui]
    [pacman.board :as board]
    [pacman.util :as util]
    [pacman.keys :as keyz]
    [pacman.tile :as _tile]
    [goog.Timer :as timer]))

(def deltas {:west [-1 0] :east [1 0] :north [0 -1] :south [0 1] :none [0 0]})
(def pacman-start {:pos (_tile/left (_tile/tile 14 26)) :face :west})

(defn get-new-face [x y kp old-face board]
  (let [[tx ty] (_tile/tile-at x y)]
    (if (_tile/tile-center? x y) ; TODO - allow fast cornering
      (cond
        ;; Reverse direction
        (and (= :west kp) (= :east old-face)) :west
        (and (= :east kp) (= :west old-face)) :east
        (and (= :north kp) (= :south old-face)) :north
        (and (= :south kp) (= :north old-face)) :south

        ;; Change direction
        ;; TODO - make a memoized function (is-open? x y)
        (and (= :north kp) (contains? (board [tx (- ty 1)]) :open)) (do (keyz/consume!) kp)
        (and (= :south kp) (contains? (board [tx (+ ty 1)]) :open)) (do (keyz/consume!) kp)
        (and (= :east kp) (contains? (board [(+ tx 1) ty]) :open)) (do (keyz/consume!) kp)
        (and (= :west kp) (contains? (board [(- tx 1) ty]) :open)) (do (keyz/consume!) kp)

        ;; Keep going (if we can)
        (and (= :north old-face) (contains? (board [tx (- ty 1)]) :open)) old-face
        (and (= :south old-face) (contains? (board [tx (+ ty 1)]) :open)) old-face
        (and (= :east old-face) (contains? (board [(+ tx 1) ty]) :open)) old-face
        (and (= :west old-face) (contains? (board [(- tx 1) ty]) :open)) old-face

        :else :none)
      old-face)))

(defn new-tile? [ox oy nx ny]
  (not= (_tile/tile-at ox oy) (_tile/tile-at nx ny)))

(defn update-pacman [old kp board]
  (let [[x y] (old :pos)
        new-face (get-new-face x y kp (old :face) board)
        [dx dy] (deltas new-face)
        [nx ny] [(mod (+ 224 x dx) 224) (+ y dy)]]

    (ui/put-pacman! [nx ny] new-face)

    (if (new-tile? x y nx ny)
      (do
        (ui/eat-at! (_tile/tile-at nx ny)))) ; TODO - call tile-at less

    (assoc old
      :face new-face
      :pos [nx ny])))


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


