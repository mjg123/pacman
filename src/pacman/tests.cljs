(ns pacman.tests
  (:require
    [pacman.util :as util]
    [pacman.core :as core]))

(defn assert-equal [msg a b]
  (util/permlog (pr-str [msg (if (= a b) :PASS :FAIL)])))

(defn run-all []
  (assert-equal "tick increases by one in update-state" (:tick (core/next-state {:tick 0} nil)) 1 ))

(run-all)
