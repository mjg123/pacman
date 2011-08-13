(ns pacman.util
  (:require [goog.dom :as dom]
    [goog.date :as date]))

(def log-elem (dom/getElement "log"))
(def debug-elem (dom/getElement "debug"))

(defn log [msg]
  (set! (.innerHTML debug-elem) msg))

(defn permlog [msg]
  (let [old-content (.innerHTML log-elem)]
    (set! (.innerHTML log-elem) (str (pr-str [ (. (date/DateTime.) (toIsoString)) msg]) "<br/>" old-content))))

