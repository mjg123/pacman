(ns pacman.util
  (:require [goog.dom :as dom]))

(defn show [msg]
  (let [data-as-json ((js* "JSON.stringify") msg nil 4)]
    ((js* "alert") data-as-json)))

(defn clj->js
  "makes a javascript map from a clojure one"
  [cljmap]
  (let [out (js-obj)]
    (doall (map #(aset out (name (first %)) (second %)) cljmap))
    out))

(defn log [msg]
  (let [val (.innerHTML (dom/getElement "log"))]
    (set! (.innerHTML (dom/getElement "log")) (str msg "<br/>" val))))

(defn debug [msg]
  (set! (.innerHTML (dom/getElement "debug")) msg))

