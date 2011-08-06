(ns pacman
  (:require [goog.graphics :as gfx]
            [goog.dom :as dom]))

(def pacman-start {:pos [112 220] :face :west})

(def pellet-pos [[1 5][2 5][3 5][4 5][5 5][6 5][7 5][8 5][9 5][10 5][11 5][12 5][1 6][1 8][1 9][1 10][1 11][1 12]])

(def energy-pos [[1 7][26 7][1 27][26 27]])










;; Graphics stuff

(def pellet-color "#FFB9AF")
(def maze-color   "#00F")
(def pacman-fill (gfx/SolidFill. "#FF0"))

;TODO: delete this!
(defn tile-center [n]
  (+ 4 (* 8 n)))

(defn tile [x y]
  [x y])

(defn pixel-pos [tile]
  (map #(* 8 %) tile))

(defn offset [off pos]
 (map #(+ %1 %2) off pos))

(let [l 0, m 3, r 7, t l, b r]
  (defn middle        [tile] (offset [m m] (pixel-pos tile)))
  (defn top-left      [tile] (offset [t l] (pixel-pos tile)))
  (defn bottom [tile] (offset [m b] (pixel-pos tile)))
  (defn bottom-left   [tile] (offset [l b] (pixel-pos tile)))
  (defn top    [tile] (offset [m t] (pixel-pos tile)))
  (defn bottom-right  [tile] (offset [r b] (pixel-pos tile)))
  (defn top-right     [tile] (offset [r t] (pixel-pos tile)))
  (defn right  [tile] (offset [r m] (pixel-pos tile)))
  (defn left   [tile] (offset [l m] (pixel-pos tile)))
)

(defn black-background [field]
  (.drawRect field 0 0 (.width field) (.height field) nil (gfx/SolidFill. "#000")))

(defn moveTo [path coords]
  (.moveTo path (first coords) (second coords)))

(defn lineTo [path coords]
  (.lineTo path (first coords) (second coords)))

(defn arcTo [path coords]
  (let [cur (.currentPoint_ path)]
    (.arcTo path (- (first coords) (first cur)) (- (second coords) (second cur)) -90 90)))

(defn larcTo [path coords]
  (let [cur (.currentPoint_ path)]
    (.arcTo path (- (first coords) (first cur)) (- (second coords) (second cur)) 180 -90)))

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


(defn draw-maze [field]
  (let [path (gfx/Path.)]
    (-> path
      (moveTo (top-left (tile 0 17)))
      (lineTo (top-left (tile 5 17)))
      (lineTo (bottom-left (tile 5 13)))
      (lineTo (bottom (tile 0 13)))
      (arcTo (left (tile 0 13)))
      (lineTo (left (tile 0 4)))
      (arcTo (top (tile 0 4)))
      (lineTo (top (tile 27 4)))
      (arcTo (right (tile 27 4)))
      (lineTo (right (tile 27 13)))
      (arcTo (bottom (tile 27 13)))
      (lineTo (bottom-right (tile 22 13)))
      (lineTo (top-right (tile 22 17)))
      (lineTo (top-right (tile 27 17))))
    (.drawPath field path (gfx/Stroke. 1 maze-color)))
  (let [path (gfx/Path.)]
    (-> path
      (moveTo (left (tile 0 17)))
      (lineTo (left (tile 5 17)))
      (larcTo (top (tile 5 17)))
      (lineTo (bottom (tile 5 13)))
      (larcTo (left (tile 5 13)))
      (lineTo (right (tile 0 13)))
      (arcTo (top (tile 0 13)))
      (lineTo (bottom (tile 0 4)))
      (arcTo (right (tile 0 4)))
      (lineTo (left (tile 13 4)))
      (arcTo (bottom (tile 13 4)))
      (lineTo (top (tile 13 8)))
      (larcTo (right (tile 13 8)))
      (larcTo (top (tile 14 8)))
      (lineTo (bottom (tile 14 4)))
      (arcTo (right (tile 14 4)))
      (lineTo (left (tile 27 4)))
      (arcTo (bottom (tile 27 4)))
      (lineTo (top (tile 27 13)))
      (arcTo (left (tile 27 13)))
      (lineTo (right (tile 22 13)))
      (larcTo (bottom (tile 22 13)))
      (lineTo (top (tile 22 17)))
      (larcTo (right (tile 22 17)))
      (lineTo (right (tile 27 17))))
    (.drawPath field path (gfx/Stroke. 1 maze-color)))

    (.drawPath field (island 2 6 5 8) (gfx/Stroke. 1 maze-color))
    (.drawPath field (island 7 6 11 8) (gfx/Stroke. 1 maze-color))
    (.drawPath field (island 16 6 20 8) (gfx/Stroke. 1 maze-color))
    (.drawPath field (island 22 6 25 8) (gfx/Stroke. 1 maze-color))
    (.drawPath field (island 2 10 5 11) (gfx/Stroke. 1 maze-color))
    (.drawPath field (island 22 10 25 11) (gfx/Stroke. 1 maze-color))
    (.drawPath field (island 7 19 8 23) (gfx/Stroke. 1 maze-color))
    (.drawPath field (island 19 19 20 23) (gfx/Stroke. 1 maze-color))
    (.drawPath field (island 7 25 11 26) (gfx/Stroke. 1 maze-color))
    (.drawPath field (island 16 25 20 26) (gfx/Stroke. 1 maze-color))

    (let [path (gfx/Path.)]
      (-> path
        (moveTo (middle (tile 10 16)))
        (lineTo (middle (tile 17 16)))
        (lineTo (middle (tile 17 20)))
        (lineTo (middle (tile 10 20)))
        (lineTo (middle (tile 10 16))))
      (.drawPath field path (gfx/Stroke. 1 maze-color)))

    (let [path (gfx/Path.)]
      (-> path
        (moveTo (bottom-right (tile 10 16)))
        (lineTo (bottom-left (tile 17 16)))
        (lineTo (top-left (tile 17 20)))
        (lineTo (top-right (tile 10 20)))
        (lineTo (bottom-right (tile 10 16))))
      (.drawPath field path (gfx/Stroke. 1 maze-color)))

    (let [path (gfx/Path.)]
      (-> path
        (moveTo (right (tile 10 10)))
        (lineTo (left (tile 17 10)))
        (arcTo (bottom (tile 17 10)))
        (arcTo (left (tile 17 11)))
        (lineTo (right (tile 14 11)))
        (larcTo (bottom (tile 14 11)))
        (lineTo (top (tile 14 14)))
        (arcTo (left (tile 14 14)))
        (arcTo (top (tile 13 14)))
        (lineTo (bottom (tile 13 11)))
        (larcTo (left (tile 13 11)))
        (lineTo (right (tile 10 11)))
        (arcTo (top (tile 10 11)))
        (arcTo (right (tile 10 10))))
      (.drawPath field path (gfx/Stroke. 1 maze-color)))

    (let [path (gfx/Path.)]
      (-> path
        (moveTo (bottom (tile 7 10)))
        (arcTo (right (tile 7 10)))
        (lineTo (left (tile 8 10)))
        (arcTo (bottom (tile 8 10)))
        (lineTo (top (tile 8 13)))
        (larcTo (right (tile 8 13)))
        (lineTo (left (tile 11 13)))
        (arcTo (bottom (tile 11 13)))
        (lineTo (top (tile 11 14)))
        (arcTo (left (tile 11 14)))
        (lineTo (right (tile 8 14)))
        (larcTo (bottom (tile 8 14)))
        (lineTo (top (tile 8 17)))
        (arcTo (left (tile 8 17)))
        (lineTo (right (tile 7 17)))
        (arcTo (top (tile 7 17)))
        (lineTo (bottom (tile 7 10)))
)
      (.drawPath field path (gfx/Stroke. 1 maze-color)))


)
    

(defn draw-pellets [field poss]
  (let [pellet-stroke (gfx/Stroke. 1 pellet-color)
        pellet-fill   (gfx/SolidFill. pellet-color)]
    (doall (map 
      #(.drawCircle field (tile-center (first %)) (tile-center (second %)) 1 pellet-stroke pellet-fill)
      poss))))

(defn draw-energy [field poss]
  (let [pellet-stroke (gfx/Stroke. 1 pellet-color)
        pellet-fill   (gfx/SolidFill. pellet-color)]
    (doall (map 
      #(.drawCircle field (tile-center (first %)) (tile-center (second %)) 4 pellet-stroke pellet-fill)
      poss))))

(defn draw-pacman [field data]
  (.drawCircle field (first (data :pos)) (second (data :pos)) 8 nil pacman-fill))

(defn create-playfield []
  (let [field (gfx/createGraphics 224 288)]

    (black-background field)
    (draw-maze field)
    (draw-pellets field pellet-pos)
    (draw-energy field energy-pos)
    (draw-pacman field pacman-start)

    (.render field (dom/getElement "playfield"))))



(create-playfield)

