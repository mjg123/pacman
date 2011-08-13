(ns pacman.board
  (:require [pacman.util :as util]))

(def board-str [
  "                            "
  "                            "
  "                            "
  "                            "
  " pppppppppppp  pppppppppppp "
  " p    p     p  p     p    p "
  " e    p     p  p     p    e "
  " p    p     p  p     p    p "
  " pppppppppppppppppppppppppp "
  " p    p  p        p  p    p "
  " p    p  p        p  p    p "
  " pppppp  pppp  pppp  pppppp "
  "      p     o  o     p      "
  "      p     o  o     p      "
  "      p  oooooooooo  p      "
  "      p  o        o  p      "
  "      p  o gggggg o  p      "
  "oooooopooo gggggg ooopoooooo"
  "      p  o gggggg o  p      "
  "      p  o        o  p      "
  "      p  oooooooooo  p      "
  "      p  o        o  p      "
  "      p  o        o  p      "
  " pppppppppppp  pppppppppppp "
  " p    p     p  p     p    p "
  " p    p     p  p     p    p "
  " epp  pppppppooppppppp  ppe "
  "   p  p  p        p  p  p   "
  "   p  p  p        p  p  p   "
  " pppppp  pppp  pppp  pppppp "
  " p          p  p          p "
  " p          p  p          p "
  " pppppppppppppppppppppppppp "
  "                            "
  "                            "
  "                            "
  ])

(defn range
  ([n] (range n '()))
  ([n l] (if (= 0 n) l (recur (dec n) (conj l (- n 1))))))

(defn tile-open? [[x y]]
  (contains? (board [x y]) :open))

(defn prop [tile prop-name]
  (get-in board [tile prop-name]))

(defn- calc-exits [[x y] board]
  (filter #(not= nil %)
    ;ordered so that ghosts choose (up > down > left > right) in tie-breaks
    [(if (contains? (board [x (- y 1)]) :open) :north)
     (if (contains? (board [x (+ y 1)]) :open) :south)
     (if (contains? (board [(- x 1) y]) :open) :west)
     (if (contains? (board [(+ x 1) y]) :open) :east)]))

(defn- is-lr-tile? [x y]
  (some #{[x y]} [[12 26][15 26][12 14][15 14]]))

(defn load []
  (let [board (atom {[-1 17] {:open nil} [28 17] {:open nil}})] ; this is lazy FP!
    (doall (for [y (range (count board-str))
                 x (range (count (board-str 0)))]
      (let [sq (get-in board-str [y x])]
        (swap! board assoc [x y]
          (cond
            (= sq \ ) {}
            (= sq \p) {:open nil :food :pellet}
            (= sq \o) {:open nil :food :no-food}
            (= sq \e) {:open nil :food :energy}
            (= sq \g) {})))))

    (doall (for [y (range (count board-str))
                 x (range (count (board-str 0)))]
      (let [all-exits (calc-exits [x y] @board)
            ghost-exits (if (is-lr-tile? x y) '(:west :east) all-exits)]
        (swap! board assoc [x y]
          (assoc (@board [x y])
            :exits all-exits
            :ghost-exits ghost-exits)))))

    (def board @board)
    @board)) ; TODO - keep this, *and* return it??  Methods that use the returned value should probably call this ns
