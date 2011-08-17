(ns pacman.ghosts
  (:require [pacman.util :as util]
    [pacman.tile :as tile]))

(def deltas {:west [-1 0] :east [1 0] :north [0 -1] :south [0 1]})

(def start-tiles {
  :blinky (tile/tile 14 14)
  :pinky (tile/tile 14 17)
  :inky (tile/tile 12 17)
  :clyde (tile/tile 16 17)
  })

(defn init []
  {:blinky {:pos (tile/left (start-tiles :blinky))
            :tile (start-tiles :blinky)
            :target-tile (tile/tile 25 0)
            :home (tile/tile 25 0)
            :face :west ; TODO - choose starting face better?
            :in-da-house false
            :fright-ticks 0
            :dead false
            :next-turn :none}
   :pinky {:pos (tile/left (start-tiles :pinky))
           :tile (start-tiles :pinky)
           :target-tile (tile/tile 2 0)
           :home (tile/tile 2 0)
           :face :south ; TODO - choose starting face better?
           :in-da-house true
           :fright-ticks 0
           :dead false
           :next-turn :none}
   :inky {:pos (tile/left (start-tiles :inky))
          :tile (start-tiles :inky)
          :target-tile (tile/tile 27 35)
          :home (tile/tile 27 35)
          :face :north ; TODO - choose starting face better?
          :in-da-house true
          :fright-ticks 0
          :dead false
          :next-turn :none}
   :clyde {:pos (tile/left (start-tiles :clyde))
           :tile (start-tiles :clyde)
           :target-tile (tile/tile 0 35)
           :home (tile/tile 0 35)
           :face :north ; TODO - choose starting face better?
           :in-da-house true
           :fright-ticks 0
           :dead false
           :next-turn :none}})

(defn offset [[x1 y1] [x2 y2]]
  [(+ x1 x2) (+ y1 y2)])

(defn dist-sq [[x1 y1] [x2 y2]]
  (+ (* (- x1 x2) (- x1 x2)) (* (- y1 y2) (- y1 y2))))

(defn choose-exit [tile target choices]
  (let [distances (zipmap choices (map #(dist-sq target (offset tile (deltas %))) choices))]
    (first (reduce #(if (< (second %2) (second %1)) %2 %1) '(:too-big 1000000) distances))))

(defn pinky-target [{[px py] :tile pface :face}]
  ; replicates the bug in pinky's targeting while pacman is going north.
  (let [pman-vec (if (= :north pface) [-1 -1] (deltas pface))
        [vx vy] (map #(* 4 %) pman-vec)]
    [(+ px vx) (+ py vy)]))

(defn inky-target [{[px py] :tile pface :face} {[bx by] :tile}]
  ; same bug in inky's targeting while pacman is going north.
  (let [pman-vec (if (= :north pface) [-1 -1] (deltas pface)) ;vector of pacman's travel
        [vx vy] (map #(* 2 %) pman-vec) ; * 2
        [sx sy] [(+ px vx) (+ py vy)] ; drawn relative to pacman
        [bvx bvy] [(- sx bx) (- sy by)]] ; vector from blinky to [sx sy]
    [(+ bx (* 2 bvx)) (+ by (* 2 bvy))])) ; twice that vector, starting at [bx by]

(defn clyde-target [{[px py] :tile} {[cx cy] :tile [hx hy] :home}]
  (if (> 64 (+ (* (- px cx) (- px cx)) (* (- py cy) (- py cy))))
    [hx hy]
    [px py]))