(ns pacman.levels)

(defn tick-at [s]
  (* 60 s)) ; todo - why 60?  We're running at 30 fps...

(def level-info-data
  {:pacman-speed 0.8
   :ghost-speed 0.75
   :ghost-tunnel-speed 0.4
   :ghost-house-speed 0.5
   :ghost-mode-times {
     (tick-at 0) :scatter
     (tick-at 7) :chase
     (tick-at 27) :scatter
     (tick-at 34) :chase
     (tick-at 54) :scatter
     (tick-at 59) :chase
     (tick-at 79) :scatter
     (tick-at 84) :chase}
   :ghost-house-dotcounts {
     :blinky 0
     :pinky 0
     :inky 30
     :clyde 90
     }})

(defn level-info [lev]
  level-info-data)