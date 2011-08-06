(ns pacman
  (:require [goog.graphics :as gfx]
            [goog.dom :as dom]))

(def pacman-start {:pos [112 220] :face :west})

(def pellet-pos [[1 5][2 5][3 5][4 5][5 5][6 5][7 5][8 5][9 5][10 5][11 5][12 5]
  [1 6][1 8][1 9][1 10][1 11][1 12]])











;; Graphics stuff

(def pellet-color "#FA0")
(def pacman-fill (gfx/SolidFill. "#FF0"))

(defn tile-center [n]
  (+ 4 (* 8 n)))

(defn black-background [field]
  (.drawRect field 0 0 (.width field) (.height field) nil (gfx/SolidFill. "#000")))

(defn draw-pellets [field poss]
  (let [pellet-stroke (gfx/Stroke. 1 pellet-color)
        pellet-fill   (gfx/SolidFill. pellet-color)]
    (doall (map 
      #(.drawCircle field (tile-center (first %)) (tile-center (second %)) 1 pellet-stroke pellet-fill)
      pellet-pos))))

(defn draw-pacman [field data]
  (.drawCircle field (first (data :pos)) (second (data :pos)) 8 nil pacman-fill))

(defn create-playfield []
  (let [field (gfx/createGraphics 224 288)]

    (black-background field)
    (draw-pellets field pellet-pos)
    (draw-pacman field pacman-start)

    (.render field (dom/getElement "playfield"))))



(create-playfield)

