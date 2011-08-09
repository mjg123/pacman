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

    ;(.setFill path pacman-fill)
    (def pacman-elem (.drawPath field path (gfx/Stroke. 1 "#FF0")))))

(def faces {:north 0 :south 180 :east 90 :west 270 :none 0})

(defn put-pacman! [[x y] face]
  ; TODO - set his face!
  (.setTransformation pacman-elem x y (faces face) 0 0))

(defn initialize [board pman]
  (let [field (gfx/createGraphics 224 288)]

    (black-background field)
    (maze/draw-maze field maze-color)

    (let [edibles (atom {})]
      (draw-edibles field board :pellet draw-pellet edibles)
      (draw-edibles field board :energy draw-energy edibles)
      (def edible-elems @edibles))

    (create-pacman-elem field pman)

    (.render field (dom/getElement "playfield"))))

