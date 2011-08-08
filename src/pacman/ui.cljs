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

(defn create-pacman-elem [field pman]
  (let [[x y] (pman :pos)
        elem (.drawCircle field x y 6 nil pacman-fill)]
    (def pacman-elem elem)))

(defn eat-at! [loc]
  (let [edible (edible-elems loc)]
    (if (not (nil? edible))
      (.setFill edible eaten-fill))))

(defn put-pacman! [[x y] face]
  ; TODO - set his face!
  (.setCenter pacman-elem x y))

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

