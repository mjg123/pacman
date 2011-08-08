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
    (if (_tile/tile-center? x y)
      (cond
        (and (= :north kp) (contains? (board [tx (- ty 1)]) :open)) (do (keyz/consume!) kp)
        (and (= :south kp) (contains? (board [tx (+ ty 1)]) :open)) (do (keyz/consume!) kp)
        (and (= :east kp) (contains? (board [(+ tx 1) ty]) :open)) (do (keyz/consume!) kp)
        (and (= :west kp) (contains? (board [(- tx 1) ty]) :open)) (do (keyz/consume!) kp)
        (and (= :north old-face) (contains? (board [tx (- ty 1)]) :open)) old-face
        (and (= :south old-face) (contains? (board [tx (+ ty 1)]) :open)) old-face
        (and (= :east old-face) (contains? (board [(+ tx 1) ty]) :open)) old-face
        (and (= :west old-face) (contains? (board [(- tx 1) ty]) :open)) old-face
        :else :none)
      old-face)))

(defn update-pacman [old kp board]
  (let [[x y] (old :pos)
        ;[tx ty] (_tile/tile-at x y)
        new-face (get-new-face x y kp (old :face) board)
        [dx dy] (deltas new-face)
        new-pos [(mod (+ 224 x dx) 224) (+ y dy)]]
    ;(util/debug (pr-str [tx ty]))
    (ui/put-pacman! new-pos new-face)
    (assoc old
      :face new-face
      :pos new-pos)))


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


