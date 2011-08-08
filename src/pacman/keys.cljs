(ns pacman.keys
  (:require [goog.events :as events]
    [goog.events.KeyCodes :as key-codes]
    [goog.events.KeyHandler :as key-handler]))

(def keypress (atom nil))

(defn kp []
  @keypress)

(defn consume! []
  (reset! keypress nil))

(defn handle-key [key]
  (let [code (.keyCode key)]
    (cond
      (= code key-codes/UP) (reset! keypress :north)
      (= code key-codes/DOWN) (reset! keypress :south)
      (= code key-codes/LEFT) (reset! keypress :west)
      (= code key-codes/RIGHT) (reset! keypress :east))))

(defn listen []
  (events/listen
    (events/KeyHandler. (js* "document"))
    "key"
    handle-key))
