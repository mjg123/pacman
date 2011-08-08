(ns pacman.pacui
  (:require [goog.dom :as dom]
            [goog.graphics :as gfx]))

(defn black-background [field]
  (.drawRect field 0 0 (.width field) (.height field) nil (gfx/SolidFill. "#000")))

(defn initialize []
  (let [field (gfx/createGraphics 224 288)]
    (black-background field)
    (.render field (dom/getElement "playfield"))))

