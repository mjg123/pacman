(ns pacman.util
  (:require [goog.dom :as dom]))

(defn log [msg]
  (set! (.innerHTML (dom/getElement "debug")) msg))

