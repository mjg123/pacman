(ns pacman.levels)

(defn tick-at [s]
  (* 60 s)) ; todo - why 60?  We're running at 30 fps...

(def scatter-chase-times
  {:one {
    (tick-at 0) :scatter
    (tick-at 7) :chase
    (tick-at 27) :scatter
    (tick-at 34) :chase
    (tick-at 54) :scatter
    (tick-at 59) :chase
    (tick-at 79) :scatter
    (tick-at 84) :chase}
   :two-three-four {
     (tick-at 0) :scatter
     (tick-at 7) :chase
     (tick-at 27) :scatter
     (tick-at 34) :chase
     (tick-at 54) :scatter
     (tick-at 59) :chase
     (tick-at 1092) :scatter
     (+ (tick-at 1092) 1) :chase}
   :five-plus {
     (tick-at 0) :scatter
     (tick-at 5) :chase
     (tick-at 25) :scatter
     (tick-at 30) :chase
     (tick-at 50) :scatter
     (tick-at 55) :chase
     (tick-at 1092) :scatter
     (+ (tick-at 1092) 1) :chase}}
  )

(def ghost-house-leave-times
  {:one {:blinky 0 :pinky 0 :inky 30 :clyde 90}
   :two {:blinky 0 :pinky 0 :inky 0 :clyde 50}
   :more {:blinky 0 :pinky 0 :inky 0 :clyde 0}})

(def level-info-data
  {
    1 {:pacman-speed 0.8 :ghost-speed 0.75 :ghost-tunnel-speed 0.4 :ghost-house-speed 0.5 :ghost-fright-speed 0.5
       :ghost-mode-times (scatter-chase-times :one)
       :ghost-house-dotcounts (ghost-house-leave-times :one)
       :ghost-fright-time (tick-at 6)
       }

    2 {:pacman-speed 0.9 :ghost-speed 0.85 :ghost-tunnel-speed 0.45 :ghost-house-speed 0.5 :ghost-fright-speed 0.55
       :ghost-mode-times (scatter-chase-times :two-three-four)
       :ghost-house-dotcounts (ghost-house-leave-times :two)
       :ghost-fright-time (tick-at 5)
       }

    3 {:pacman-speed 0.9 :ghost-speed 0.85 :ghost-tunnel-speed 0.45 :ghost-house-speed 0.5 :ghost-fright-speed 0.55
       :ghost-mode-times (scatter-chase-times :two-three-four)
       :ghost-house-dotcounts (ghost-house-leave-times :more)
       :ghost-fright-time (tick-at 4)
       }

    4 {:pacman-speed 0.9 :ghost-speed 0.85 :ghost-tunnel-speed 0.45 :ghost-house-speed 0.5 :ghost-fright-speed 0.55
       :ghost-mode-times (scatter-chase-times :two-three-four)
       :ghost-house-dotcounts (ghost-house-leave-times :more)
       :ghost-fright-time (tick-at 3)
       }

    5 {:pacman-speed 1 :ghost-speed 0.95 :ghost-tunnel-speed 0.5 :ghost-house-speed 0.5 :ghost-fright-speed 0.6
       :ghost-mode-times (scatter-chase-times :five-plus)
       :ghost-house-dotcounts (ghost-house-leave-times :more)
       :ghost-fright-time (tick-at 2)
       }

    6 {:pacman-speed 1 :ghost-speed 0.95 :ghost-tunnel-speed 0.5 :ghost-house-speed 0.5 :ghost-fright-speed 0.6
       :ghost-mode-times (scatter-chase-times :five-plus)
       :ghost-house-dotcounts (ghost-house-leave-times :more)
       :ghost-fright-time (tick-at 5)
       }

    7 {:pacman-speed 1 :ghost-speed 0.95 :ghost-tunnel-speed 0.5 :ghost-house-speed 0.5 :ghost-fright-speed 0.6
       :ghost-mode-times (scatter-chase-times :five-plus)
       :ghost-house-dotcounts (ghost-house-leave-times :more)
       :ghost-fright-time (tick-at 2)
       }

    8 {:pacman-speed 1 :ghost-speed 0.95 :ghost-tunnel-speed 0.5 :ghost-house-speed 0.5 :ghost-fright-speed 0.6
       :ghost-mode-times (scatter-chase-times :five-plus)
       :ghost-house-dotcounts (ghost-house-leave-times :more)
       :ghost-fright-time (tick-at 2)
       }

    9 {:pacman-speed 1 :ghost-speed 0.95 :ghost-tunnel-speed 0.5 :ghost-house-speed 0.5 :ghost-fright-speed 0.6
       :ghost-mode-times (scatter-chase-times :five-plus)
       :ghost-house-dotcounts (ghost-house-leave-times :more)
       :ghost-fright-time (tick-at 1)
       }

    10 {:pacman-speed 1 :ghost-speed 0.95 :ghost-tunnel-speed 0.5 :ghost-house-speed 0.5 :ghost-fright-speed 0.6
       :ghost-mode-times (scatter-chase-times :five-plus)
       :ghost-house-dotcounts (ghost-house-leave-times :more)
       :ghost-fright-time (tick-at 5)
       }

    11 {:pacman-speed 1 :ghost-speed 0.95 :ghost-tunnel-speed 0.5 :ghost-house-speed 0.5 :ghost-fright-speed 0.6
       :ghost-mode-times (scatter-chase-times :five-plus)
       :ghost-house-dotcounts (ghost-house-leave-times :more)
       :ghost-fright-time (tick-at 2)
       }

    12 {:pacman-speed 1 :ghost-speed 0.95 :ghost-tunnel-speed 0.5 :ghost-house-speed 0.5 :ghost-fright-speed 0.6
       :ghost-mode-times (scatter-chase-times :five-plus)
       :ghost-house-dotcounts (ghost-house-leave-times :more)
       :ghost-fright-time (tick-at 1)
       }

    13 {:pacman-speed 1 :ghost-speed 0.95 :ghost-tunnel-speed 0.5 :ghost-house-speed 0.5 :ghost-fright-speed 0.6
       :ghost-mode-times (scatter-chase-times :five-plus)
       :ghost-house-dotcounts (ghost-house-leave-times :more)
       :ghost-fright-time (tick-at 1)
       }

    14 {:pacman-speed 1 :ghost-speed 0.95 :ghost-tunnel-speed 0.5 :ghost-house-speed 0.5 :ghost-fright-speed 0.6
       :ghost-mode-times (scatter-chase-times :five-plus)
       :ghost-house-dotcounts (ghost-house-leave-times :more)
       :ghost-fright-time (tick-at 3)
       }

    15 {:pacman-speed 1 :ghost-speed 0.95 :ghost-tunnel-speed 0.5 :ghost-house-speed 0.5 :ghost-fright-speed 0.6
       :ghost-mode-times (scatter-chase-times :five-plus)
       :ghost-house-dotcounts (ghost-house-leave-times :more)
       :ghost-fright-time (tick-at 1)
       }

    16 {:pacman-speed 1 :ghost-speed 0.95 :ghost-tunnel-speed 0.5 :ghost-house-speed 0.5 :ghost-fright-speed 0.6
       :ghost-mode-times (scatter-chase-times :five-plus)
       :ghost-house-dotcounts (ghost-house-leave-times :more)
       :ghost-fright-time (tick-at 1)
       }

    17 {:pacman-speed 1 :ghost-speed 0.95 :ghost-tunnel-speed 0.5 :ghost-house-speed 0.5 :ghost-fright-speed 0.6
       :ghost-mode-times (scatter-chase-times :five-plus)
       :ghost-house-dotcounts (ghost-house-leave-times :more)
       :ghost-fright-time 0
       }

    18 {:pacman-speed 1 :ghost-speed 0.95 :ghost-tunnel-speed 0.5 :ghost-house-speed 0.5 :ghost-fright-speed 0.6
       :ghost-mode-times (scatter-chase-times :five-plus)
       :ghost-house-dotcounts (ghost-house-leave-times :more)
       :ghost-fright-time (tick-at 1)
       }

    19 {:pacman-speed 1 :ghost-speed 0.95 :ghost-tunnel-speed 0.5 :ghost-house-speed 0.5 :ghost-fright-speed 0.6
       :ghost-mode-times (scatter-chase-times :five-plus)
       :ghost-house-dotcounts (ghost-house-leave-times :more)
       :ghost-fright-time 0
       }

    20 {:pacman-speed 1 :ghost-speed 0.95 :ghost-tunnel-speed 0.5 :ghost-house-speed 0.5 :ghost-fright-speed 0.6
       :ghost-mode-times (scatter-chase-times :five-plus)
       :ghost-house-dotcounts (ghost-house-leave-times :more)
       :ghost-fright-time 0
       }

    21 {:pacman-speed 0.9 :ghost-speed 0.95 :ghost-tunnel-speed 0.5 :ghost-house-speed 0.5 :ghost-fright-speed 0.6
       :ghost-mode-times (scatter-chase-times :five-plus)
       :ghost-house-dotcounts (ghost-house-leave-times :more)
       :ghost-fright-time 0
       }

    })

(defn level-info [lev]
  (get level-info-data lev (level-info-data 21)))