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
(def ghost-colors {:blinky (gfx/SolidFill. "#F00")
                   :pinky (gfx/SolidFill. "#ffb8ff")
                   :inky (gfx/SolidFill. "#0FF")
                   :clyde (gfx/SolidFill. "#ffb851")
                   :frightened (gfx/SolidFill. "#448")
                   :recovering (gfx/SolidFill. "#FFF")
                   :dead (gfx/SolidFill. "#FFF0")})

(defn black-background [field]
  (.drawRect field 0 0 (.width field) (.height field) nil (gfx/SolidFill. "#000")))

(defn draw-pellet [field tile]
  (let [[x y] (tile/middle tile)]
    (.drawCircle field x y 1 nil pellet-fill)))

(defn draw-energy [field tile]
  (let [[x y] (tile/middle tile)]
    (.drawCircle field x y 3 nil pellet-fill)))

(defn has-food? [[coord tile-data] food-name]
  (= (tile-data :food) food-name))

(defn draw-edibles [field board edible-name draw-fn edibles]
  (let [locations (keys (filter #(has-food? % edible-name) board))]
    (doall (map
      #(swap! edibles assoc % (draw-fn field %))
      locations))))

(defn eat-at! [loc]
  (let [edible (edible-elems loc)]
    (if (not (nil? edible))
      (.setFill edible eaten-fill))))

(defn reset-edibles! []
  (doall (map #(.setFill (second %) pellet-fill) edible-elems)))

(defn create-pacman-elems [field pman]
  (let [[x y] (pman :pos)
        path1 (gfx/Path.)
        path2 (gfx/Path.)]

    (.moveTo path1 0 0)
    (.arc path1 0 0 5.6 5.6 45 180)
    (.lineTo path1 0 0)

    (.moveTo path2 0 0)
    (.arc path2 0 0 5.6 5.6 135 -180)
    (.lineTo path2 0 0)

    (let [stroke (gfx/Stroke. 1 "#FF0")
          fill nil ;pacman-fill
          arc1 (.drawPath field path1 stroke fill)
          arc2 (.drawPath field path2 stroke fill)]

      (def pacman-elems [arc1 arc2]))))

(def faces {:north 0 :south 180 :east 90 :west 270 :none 0})
(def eye-deltas {:north [0 -2] :south [0 0] :east [2 0] :west [-2 0] :none [0 0]})
(def pupil-deltas {:north [0 -3] :south [0 1] :east [3 0] :west [-3 0] :none [0 0]})

(defn waka-waka "angle of pacman's open mouth" [tick]
  (let [t (* 8 tick)
        m90 (mod t 90)]
    (if (< 45 m90)
      (- 90 m90)
      m90)))

(defn put-pacman! [[x y] face tick]
  (.setTransformation (first pacman-elems) x y (+ (faces face) (waka-waka tick)) 0 0)
  (.setTransformation (second pacman-elems) x y (- (faces face) (waka-waka tick)) 0 0))

(def es 2.2) ; eye-spacing

(defn put-ghost! [ghost-name [x y] face target mode]
  (let [[tx ty] (tile/middle target)
        [edx edy] (eye-deltas face)
        [pdx pdy] (pupil-deltas face)
        fill (cond (= :normal mode) (ghost-colors ghost-name) :else (ghost-colors mode))]

    (.setFill (get-in ghost-elems [ghost-name :pos]) fill)
    (.setFill (get-in ghost-elems [ghost-name :body]) fill)

    (.setCenter (get-in ghost-elems [ghost-name :pos]) x y)
    (.setPosition (get-in ghost-elems [ghost-name :body]) (- x 6) y)
    (.setCenter (get-in ghost-elems [ghost-name :target]) tx ty)
    (.setCenter (get-in ghost-elems [ghost-name :leye]) (+ x edx (- es)) (+ y edy))
    (.setCenter (get-in ghost-elems [ghost-name :lpupil]) (+ x pdx (- es)) (+ y pdy))
    (.setCenter (get-in ghost-elems [ghost-name :reye]) (+ x edx es) (+ y edy))
    (.setCenter (get-in ghost-elems [ghost-name :rpupil]) (+ x pdx es) (+ y pdy))))

(def eye-radius-x 2)
(def eye-radius-y 2.5)
(def pupil-radius-x 1)
(def pupil-radius-y 1.5)

(defn create-ghost-elems [field {[x y] :pos} color]
  {:pos (.drawEllipse field x y 6 6 nil color)
   :body (.drawRect field (- x 6) y 12 6 nil color)
   :target (.drawEllipse field -10 -10 2 2 nil color) ; TODO hide targets!
   :leye (.drawEllipse field (- x es) y eye-radius-x eye-radius-y nil (gfx/SolidFill. "#FFF"))
   :reye (.drawEllipse field (+ x es) y eye-radius-x eye-radius-y nil (gfx/SolidFill. "#FFF"))
   :lpupil (.drawEllipse field (- x es) y pupil-radius-x pupil-radius-y nil (gfx/SolidFill. "#00F"))
   :rpupil (.drawEllipse field (+ x es) y pupil-radius-x pupil-radius-y nil (gfx/SolidFill. "#00F"))})

(defn create-ghosts [field ghosts]
  (def ghost-elems {
    :blinky (create-ghost-elems field (ghosts :blinky) (ghost-colors :blinky))
    :pinky (create-ghost-elems field (ghosts :pinky) (ghost-colors :pinky))
    :inky (create-ghost-elems field (ghosts :inky) (ghost-colors :inky))
    :clyde (create-ghost-elems field (ghosts :clyde) (ghost-colors :clyde))
    }))

(def initial-score "0")

(defn update-score! [score]
  (.setText score-elem (str score)))

(defn create-score [field]
  (let [[tx ty] (tile/bottom-left (tile/tile 5 0))
        elem (.drawText field initial-score tx ty 80 8 "left" "bottom" (gfx/Font. 8 "sans-serif") nil (gfx/SolidFill. "#FFF"))]
    (def score-elem elem)))

(defn clear-ready-message! []
  (.setTransformation ready-elem 1000 1000 0 0 0))

(defn show-ready-message! [level]
  (let [msg (if (= 1 level) "READY!" (str "LEVEL " level))]
    (.setText ready-elem msg)
    (.setTransformation ready-elem 0 0 0 0 0)))

(defn initialize [board pman ghosts]
  (let [field (gfx/createGraphics 224 288)]

    (black-background field)
    (maze/draw-maze field maze-color)

    (def ready-elem (.drawText field "READY!" 90 163 80 8 "left" "bottom" (gfx/Font. 12 "sans-serif") nil (gfx/SolidFill. "#FF0")))

    (let [edibles (atom {})]
      (draw-edibles field board :pellet draw-pellet edibles)
      (draw-edibles field board :energy draw-energy edibles)
      (def edible-elems @edibles))

    (create-ghosts field ghosts)
    (create-pacman-elems field pman)

    (put-pacman! (pman :pos) (pman :face) 0)

    (create-score field)

    (.render field (dom/getElement "playfield"))))

