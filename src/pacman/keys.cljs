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
      ; emergency stop (for testing)
      (= code key-codes/CLOSE_SQUARE_BRACKET) (reset! keypress :stop)

      ; arrow keys
      (= code key-codes/UP) (reset! keypress :north)
      (= code key-codes/DOWN) (reset! keypress :south)
      (= code key-codes/LEFT) (reset! keypress :west)
      (= code key-codes/RIGHT) (reset! keypress :east)

      ; doom keys
      (= code key-codes/W) (reset! keypress :north)
      (= code key-codes/S) (reset! keypress :south)
      (= code key-codes/A) (reset! keypress :west)
      (= code key-codes/D) (reset! keypress :east)

      ; vi keys
      (= code key-codes/J) (reset! keypress :north)
      (= code key-codes/K) (reset! keypress :south)
      (= code key-codes/H) (reset! keypress :west)
      (= code key-codes/L) (reset! keypress :east))))

(defn listen []
  (events/listen
    (events/KeyHandler. (js* "document"))
    "key"
    handle-key))
