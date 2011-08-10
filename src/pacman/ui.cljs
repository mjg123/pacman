(ns pacman.ui
  (:require [goog.dom :as dom]
    [goog.graphics :as gfx]
    [pacman.util :as util]
    [pacman.maze :as maze]
    [pacman.tile :as tile]))

(def maze-color "#00F")
(def pellet-fill (gfx/SolidFill. "#FFB9AF"))
(def eaten-fill (gfx/SolidFill. "#000"))
(def pacman-fill (gfx/SolidFill. "#FF0"))
(def ghost-colors {:blinky "#F00"
                   :pinky "#ffb8ff"
                   :inky "#0FF"
                   :clyde "#ffb851"})

(defn black-background [field]
  (.drawRect field 0 0 (.width field) (.height field) nil (gfx/SolidFill. "#000")))

(defn draw-pellet [field tile]
  (let [[x y] (tile/middle tile)]
    (.drawCircle field x y 1 nil pellet-fill)))

(defn draw-energy [field tile]
  (let [[x y] (tile/middle tile)]
    (.drawCircle field x y 3 nil pellet-fill)))

(defn draw-edibles [field board edible-name draw-fn edibles]
  (let [locations (keys (filter #(contains? (second %) edible-name) board))]
    (doall (map
      #(swap! edibles assoc % (draw-fn field %))
      locations))))

(defn eat-at! [loc]
  (let [edible (edible-elems loc)]
    (if (not (nil? edible))
      (.setFill edible eaten-fill))))

(defn create-pacman-elem [field pman]
  (let [[x y] (pman :pos)
        path (gfx/Path.)]
    (.moveTo path 4 -4)
    (.arcToAsCurves path 5.6 5.6 -45 270)
    (.moveTo path 4 -4)
    (.lineTo path 0 0)
    (.lineTo path -4 -4)

    (let [elem (.drawPath field path (gfx/Stroke. 1 "#FF0"))]
      ;(.setFill elem pacman-fill) ; TODO - wakawaka
      (def pacman-elem elem))))

(def faces {:north 0 :south 180 :east 90 :west 270 :none 0})
(def eye-deltas {:north [0 0] :south [0 0] :east [2 0] :west [-2 0]})
(def pupil-deltas {:north [0 -1] :south [0 1] :east [3 0] :west [-3 0]})

(defn put-pacman! [[x y] face]
  (.setTransformation pacman-elem x y (faces face) 0 0))

(defn put-ghost! [ghost [x y] face target]
  (let [[tx ty] (tile/middle target)
        [edx edy] (eye-deltas face)
        [pdx pdy] (pupil-deltas face)]
    (.setCenter (get-in ghost-elems [ghost :pos]) x y)
    (.setPosition (get-in ghost-elems [ghost :body]) (- x 6) y)
    (.setCenter (get-in ghost-elems [ghost :target]) tx ty)
    (.setCenter (get-in ghost-elems [ghost :leye]) (+ x edx -2) (+ y edy))
    (.setCenter (get-in ghost-elems [ghost :lpupil]) (+ x pdx -2) (+ y pdy))
    (.setCenter (get-in ghost-elems [ghost :reye]) (+ x edx 2) (+ y edy))
    (.setCenter (get-in ghost-elems [ghost :rpupil]) (+ x pdx 2) (+ y pdy))))

(def eye-radius 2.5)
(def pupil-radius 1.5)

(defn create-ghost-elems [field {[x y] :pos [tx ty] :target-tile} color]
  {:pos (.drawEllipse field x y 6 6 nil (gfx/SolidFill. color))
   :body (.drawRect field (- x 6) y 12 6 nil (gfx/SolidFill. color))
   :target (.drawEllipse field tx ty 2 2 (gfx/Stroke. 1 color) nil)
   :leye (.drawEllipse field (- x 2) y eye-radius eye-radius nil (gfx/SolidFill. "#FFF"))
   :reye (.drawEllipse field (+ x 2) y eye-radius eye-radius nil (gfx/SolidFill. "#FFF"))
   :lpupil (.drawEllipse field (- x 2) y pupil-radius pupil-radius nil (gfx/SolidFill. "#00F"))
   :rpupil (.drawEllipse field (+ x 2) y pupil-radius pupil-radius nil (gfx/SolidFill. "#00F"))})

(defn create-ghosts [field ghosts]
  (def ghost-elems {
    :blinky (create-ghost-elems field (ghosts :blinky) (ghost-colors :blinky))
    :pinky (create-ghost-elems field (ghosts :pinky) (ghost-colors :pinky))
    :inky (create-ghost-elems field (ghosts :inky) (ghost-colors :inky))
    :clyde (create-ghost-elems field (ghosts :clyde) (ghost-colors :clyde))
    }))

(defn initialize [board pman ghosts]
  (let [field (gfx/createGraphics 224 288)]

    (black-background field)
    (maze/draw-maze field maze-color)

    (let [edibles (atom {})]
      (draw-edibles field board :pellet draw-pellet edibles)
      (draw-edibles field board :energy draw-energy edibles)
      (def edible-elems @edibles))

    (create-ghosts field ghosts)
    (create-pacman-elem field pman)

    (.render field (dom/getElement "playfield"))))

