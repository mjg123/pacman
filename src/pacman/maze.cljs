(ns pacman.maze
  (:require [goog.graphics :as gfx]
    [pacman.tile :as _tile]))

; importing tile namespace by hand...
(def tile _tile/tile)
(def pixel-pos _tile/pixel-pos)
(def middle _tile/middle)
(def top-left _tile/top-left)
(def bottom _tile/bottom)
(def bottom-left _tile/bottom-left)
(def top _tile/top)
(def bottom-right _tile/bottom-right)
(def top-right _tile/top-right)
(def right _tile/right)
(def left _tile/left)


(defn moveTo [path [x y]]
  (.moveTo path x y))

(defn lineTo [path [x y]]
  (.lineTo path x y))

(defn arcTo [path [x y]]
  (let [[cx cy] (.currentPoint_ path)]
    (.arcTo path (- x cx) (- y cy) -90 90)))

(defn larcTo [path [x y]]
  (let [[cx cy] (.currentPoint_ path)]
    (.arcTo path (- x cx) (- y cy) 180 -90)))

(defn island [x y r b]
  (let [path (gfx/Path.)]
    (-> path
      (moveTo (right (tile x y)))
      (lineTo (left (tile r y)))
      (arcTo (bottom (tile r y)))
      (lineTo (top (tile r b)))
      (arcTo (left (tile r b)))
      (lineTo (right (tile x b)))
      (arcTo (top (tile x b)))
      (lineTo (bottom (tile x y)))
      (arcTo (right (tile x y))))))

(defn t-shape [x y]
  (let [path (gfx/Path.)]
    (-> path
      (moveTo (right (tile x y)))
      (lineTo (left (tile (+ 7 x) y)))
      (arcTo (bottom (tile (+ 7 x) y)))
      (arcTo (left (tile (+ 7 x) (+ 1 y))))
      (lineTo (right (tile (+ 4 x) (+ 1 y))))
      (larcTo (bottom (tile (+ 4 x) (+ 1 y))))
      (lineTo (top (tile (+ 4 x) (+ 4 y))))
      (arcTo (left (tile (+ 4 x) (+ 4 y))))
      (arcTo (top (tile 13 (+ 4 y))))
      (lineTo (bottom (tile (+ 3 x) (+ 1 y))))
      (larcTo (left (tile (+ 3 x) (+ 1 y))))
      (lineTo (right (tile x (+ 1 y))))
      (arcTo (top (tile x (+ 1 y))))
      (arcTo (right (tile x y))))))


(defn draw-maze [field maze-color]
  (let [maze-stroke (gfx/Stroke. 1 maze-color)]
    (let [path (gfx/Path.)]
      (-> path
        (moveTo (top-left (tile 0 16)))
        (lineTo (top-left (tile 5 16)))
        (lineTo (bottom-left (tile 5 12)))
        (lineTo (bottom (tile 0 12)))
        (arcTo (left (tile 0 12)))
        (lineTo (left (tile 0 3)))
        (arcTo (top (tile 0 3)))
        (lineTo (top (tile 27 3)))
        (arcTo (right (tile 27 3)))
        (lineTo (right (tile 27 12)))
        (arcTo (bottom (tile 27 12)))
        (lineTo (bottom-right (tile 22 12)))
        (lineTo (top-right (tile 22 16)))
        (lineTo (top-right (tile 27 16))))
      (.drawPath field path maze-stroke))
    (let [path (gfx/Path.)]
      (-> path
        (moveTo (left (tile 0 16)))
        (lineTo (left (tile 5 16)))
        (larcTo (top (tile 5 16)))
        (lineTo (bottom (tile 5 12)))
        (larcTo (left (tile 5 12)))
        (lineTo (right (tile 0 12)))
        (arcTo (top (tile 0 12)))
        (lineTo (bottom (tile 0 3)))
        (arcTo (right (tile 0 3)))
        (lineTo (left (tile 13 3)))
        (arcTo (bottom (tile 13 3)))
        (lineTo (top (tile 13 7)))
        (larcTo (right (tile 13 7)))
        (larcTo (top (tile 14 7)))
        (lineTo (bottom (tile 14 3)))
        (arcTo (right (tile 14 3)))
        (lineTo (left (tile 27 3)))
        (arcTo (bottom (tile 27 3)))
        (lineTo (top (tile 27 12)))
        (arcTo (left (tile 27 12)))
        (lineTo (right (tile 22 12)))
        (larcTo (bottom (tile 22 12)))
        (lineTo (top (tile 22 16)))
        (larcTo (right (tile 22 16)))
        (lineTo (right (tile 27 16))))
      (.drawPath field path maze-stroke))

    (.drawPath field (island 2 5 5 7) maze-stroke)
    (.drawPath field (island 7 5 11 7) maze-stroke)
    (.drawPath field (island 16 5 20 7) maze-stroke)
    (.drawPath field (island 22 5 25 7) maze-stroke)
    (.drawPath field (island 2 9 5 10) maze-stroke)
    (.drawPath field (island 22 9 25 10) maze-stroke)
    (.drawPath field (island 7 18 8 22) maze-stroke)
    (.drawPath field (island 19 18 20 22) maze-stroke)
    (.drawPath field (island 7 24 11 25) maze-stroke)
    (.drawPath field (island 16 24 20 25) maze-stroke)

    (let [path (gfx/Path.)]
      (-> path
        (moveTo (middle (tile 10 15)))
        (lineTo (middle (tile 17 15)))
        (lineTo (middle (tile 17 19)))
        (lineTo (middle (tile 10 19)))
        (lineTo (middle (tile 10 15))))
      (.drawPath field path maze-stroke))

    (let [path (gfx/Path.)]
      (-> path
        (moveTo (bottom-right (tile 10 15)))
        (lineTo (bottom-left (tile 17 15)))
        (lineTo (top-left (tile 17 19)))
        (lineTo (top-right (tile 10 19)))
        (lineTo (bottom-right (tile 10 15))))
      (.drawPath field path maze-stroke))

    (.drawPath field (t-shape 10 9) maze-stroke)
    (.drawPath field (t-shape 10 21) maze-stroke)
    (.drawPath field (t-shape 10 27) maze-stroke)

    (let [path (gfx/Path.)]
      (-> path
        (moveTo (bottom (tile 7 9)))
        (arcTo (right (tile 7 9)))
        (lineTo (left (tile 8 9)))
        (arcTo (bottom (tile 8 9)))
        (lineTo (top (tile 8 12)))
        (larcTo (right (tile 8 12)))
        (lineTo (left (tile 11 12)))
        (arcTo (bottom (tile 11 12)))
        (lineTo (top (tile 11 13)))
        (arcTo (left (tile 11 13)))
        (lineTo (right (tile 8 13)))
        (larcTo (bottom (tile 8 13)))
        (lineTo (top (tile 8 16)))
        (arcTo (left (tile 8 16)))
        (lineTo (right (tile 7 16)))
        (arcTo (top (tile 7 16)))
        (lineTo (bottom (tile 7 9))))
      (.drawPath field path maze-stroke))

    (let [path (gfx/Path.)]
      (-> path
        (moveTo (bottom (tile 19 9)))
        (arcTo (right (tile 19 9)))
        (lineTo (left (tile 20 9)))
        (arcTo (bottom (tile 20 9)))
        (lineTo (top (tile 20 16)))
        (arcTo (left (tile 20 16)))
        (lineTo (right (tile 19 16)))
        (arcTo (top (tile 19 16)))
        (lineTo (bottom (tile 19 13)))
        (larcTo (left (tile 19 13)))
        (lineTo (right (tile 16 13)))
        (arcTo (top (tile 16 13)))
        (lineTo (bottom (tile 16 12)))
        (arcTo (right (tile 16 12)))
        (lineTo (left (tile 19 12)))
        (larcTo (top (tile 19 12)))
        (lineTo (bottom (tile 19 9))))
      (.drawPath field path maze-stroke))

    (let [path (gfx/Path.)]
      (-> path
        (moveTo (bottom (tile 2 24)))
        (arcTo (right (tile 2 24)))
        (lineTo (left (tile 5 24)))
        (arcTo (bottom (tile 5 24)))
        (lineTo (top (tile 5 28)))
        (arcTo (left (tile 5 28)))
        (lineTo (right (tile 4 28)))
        (arcTo (top (tile 4 28)))
        (lineTo (bottom (tile 4 25)))
        (larcTo (left (tile 4 25)))
        (lineTo (right (tile 2 25)))
        (arcTo (top (tile 2 25)))
        (lineTo (bottom (tile 2 24))))
      (.drawPath field path maze-stroke))

    (let [path (gfx/Path.)]
      (-> path
        (moveTo (bottom (tile 22 24)))
        (arcTo (right (tile 22 24)))
        (lineTo (left (tile 25 24)))
        (arcTo (bottom (tile 25 24)))
        (lineTo (top (tile 25 25)))
        (arcTo (left (tile 25 25)))
        (lineTo (right (tile 23 25)))
        (larcTo (bottom (tile 23 25)))
        (lineTo (top (tile 23 28)))
        (arcTo (left (tile 23 28)))
        (lineTo (right (tile 22 28)))
        (arcTo (top (tile 22 28)))
        (lineTo (bottom (tile 22 24))))
      (.drawPath field path maze-stroke))

    (let [path (gfx/Path.)]
      (-> path
        (moveTo (left (tile 0 18)))
        (lineTo (left (tile 5 18)))
        (arcTo (bottom (tile 5 18)))
        (lineTo (top (tile 5 22)))
        (arcTo (left (tile 5 22)))
        (lineTo (right (tile 0 22)))
        (larcTo (bottom (tile 0 22)))
        (lineTo (top (tile 0 27)))
        (larcTo (right (tile 0 27)))
        (lineTo (left (tile 2 27)))
        (arcTo (bottom (tile 2 27)))
        (lineTo (top (tile 2 28)))
        (arcTo (left (tile 2 28)))
        (lineTo (right (tile 0 28)))
        (larcTo (bottom (tile 0 28)))
        (lineTo (top (tile 0 33)))
        (larcTo (right (tile 0 33)))
        (lineTo (left (tile 27 33)))
        (larcTo (top (tile 27 33)))
        (lineTo (bottom (tile 27 28)))
        (larcTo (left (tile 27 28)))
        (lineTo (right (tile 25 28)))
        (arcTo (top (tile 25 28)))
        (lineTo (bottom (tile 25 27)))
        (arcTo (right (tile 25 27)))
        (lineTo (left (tile 27 27)))
        (larcTo (top (tile 27 27)))
        (lineTo (bottom (tile 27 22)))
        (larcTo (left (tile 27 22)))
        (lineTo (right (tile 22 22)))
        (arcTo (top (tile 22 22)))
        (lineTo (bottom (tile 22 18)))
        (arcTo (right (tile 22 18)))
        (lineTo (right (tile 27 18))))
      (.drawPath field path maze-stroke))

    (let [path (gfx/Path.)]
      (-> path
        (moveTo (bottom-left (tile 0 18)))
        (lineTo (bottom-left (tile 5 18)))
        (lineTo (top-left (tile 5 22)))
        (lineTo (top-right (tile 0 22)))
        (larcTo (bottom-left (tile 0 22)))
        (lineTo (top-left (tile 0 33)))
        (larcTo (bottom-right (tile 0 33)))
        (lineTo (bottom-left (tile 27 33)))
        (larcTo (top-right (tile 27 33)))
        (lineTo (bottom-right (tile 27 22)))
        (larcTo (top-left (tile 27 22)))
        (lineTo (top-right (tile 22 22)))
        (lineTo (bottom-right (tile 22 18)))
        (lineTo (bottom-right (tile 27 18))))
      (.drawPath field path maze-stroke))

    (let [path (gfx/Path.)]
      (-> path
        (moveTo (right (tile 2 30)))

        (lineTo (left (tile 7 30)))
        (larcTo (top (tile 7 30)))
        (lineTo (bottom (tile 7 27)))
        (arcTo (right (tile 7 27)))
        (lineTo (left (tile 8 27)))
        (arcTo (bottom (tile 8 27)))
        (lineTo (top (tile 8 30)))
        (larcTo (right (tile 8 30)))

        (lineTo (left (tile 11 30)))
        (arcTo (bottom (tile 11 30)))
        (lineTo (top (tile 11 31)))
        (arcTo (left (tile 11 31)))
        (lineTo (right (tile 2 31)))
        (arcTo (top (tile 2 31)))
        (lineTo (bottom (tile 2 30)))
        (arcTo (right (tile 2 30))))
      (.drawPath field path maze-stroke))

    (let [path (gfx/Path.)]
      (-> path
        (moveTo (right (tile 16 30)))

        (lineTo (left (tile 19 30)))
        (larcTo (top (tile 19 30)))
        (lineTo (bottom (tile 19 27)))
        (arcTo (right (tile 19 27)))
        (lineTo (left (tile 20 27)))
        (arcTo (bottom (tile 20 27)))
        (lineTo (top (tile 20 30)))
        (larcTo (right (tile 20 30)))

        (lineTo (left (tile 25 30)))
        (arcTo (bottom (tile 25 30)))
        (lineTo (top (tile 25 31)))
        (arcTo (left (tile 25 31)))
        (lineTo (right (tile 16 31)))
        (arcTo (top (tile 16 31)))
        (lineTo (bottom (tile 16 30)))
        (arcTo (right (tile 16 30))))
      (.drawPath field path maze-stroke))

    ))