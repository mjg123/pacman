(ns pacman.ui
  (:require [goog.dom :as dom]
    [goog.graphics :as gfx]
    [pacman.maze :as maze]
    [pacman.tile :as tile]))

(def maze-color "#00F")
(def pellet-color "#FFB9AF")
(def fake-color "#F0F")
(def pacman-fill (gfx/SolidFill. "#FF0"))

(defn black-background [field]
  (.drawRect field 0 0 (.width field) (.height field) nil (gfx/SolidFill. "#000")))

(defn draw-pellet [field tile]
  (let [pellet-stroke (gfx/Stroke. 1 pellet-color)
        pellet-fill (gfx/SolidFill. pellet-color)
        [x y] (tile/middle tile)]
    (.drawCircle field x y 1 pellet-stroke pellet-fill)))

(defn draw-energy [field tile]
  (let [pellet-stroke (gfx/Stroke. 1 pellet-color)
        pellet-fill (gfx/SolidFill. pellet-color)
        [x y] (tile/middle tile)]
    (.drawCircle field x y 3 pellet-stroke pellet-fill)))

(defn draw-edibles [field board edible-name draw-fn]
  (let [locations (keys (filter #(contains? (second %) edible-name) board))]
    (doall (map
      #(draw-fn field %)
      locations))))

(defn draw-pacman
  "Defines pacman-elem"
  [field pman]
  (let [[x y] (pman :pos)
        elem (.drawCircle field x y 6 nil pacman-fill)]
    (def pacman-elem elem)))

(defn put-pacman! [[x y] face]
  (.setCenter pacman-elem x y))

(defn initialize [board pman]
  (let [field (gfx/createGraphics 224 288)]
    (black-background field)
    (maze/draw-maze field maze-color)
    (draw-edibles field board :pellet draw-pellet)
    (draw-edibles field board :energy draw-energy)
    (draw-pacman field pman)
    (.render field (dom/getElement "playfield"))))

