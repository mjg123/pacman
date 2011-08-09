(ns pacman.ghosts
  (:require [pacman.util :as util]))

(def deltas {:west [-1 0] :east [1 0] :north [0 -1] :south [0 1]})

(defn offset [[x1 y1] [x2 y2]]
  [(+ x1 x2) (+ y1 y2)])

(defn dist-sq [[x1 y1] [x2 y2]]
  (+ (* (- x1 x2) (- x1 x2)) (* (- y1 y2) (- y1 y2))))

(defn choose-exit[tile target choices]
  (let [distances (zipmap choices (map #(dist-sq target (offset tile (deltas %))) choices))]
    (first (reduce #(if (< (second %2) (second %1)) %2 %1) '(:too-big 1000000) distances))))