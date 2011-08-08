(ns pacman.tile)

(defn tile [x y]
  [x y])

(defn pixel-pos [tile]
  (map #(* 8 %) tile))

(defn offset [off pos]
  (map #(+ %1 %2) off pos))

(let [l 0, m 4, r 7, t l, b r]
  (defn middle [tile] (offset [m m] (pixel-pos tile)))
  (defn top-left [tile] (offset [t l] (pixel-pos tile)))
  (defn bottom [tile] (offset [m b] (pixel-pos tile)))
  (defn bottom-left [tile] (offset [l b] (pixel-pos tile)))
  (defn top [tile] (offset [m t] (pixel-pos tile)))
  (defn bottom-right [tile] (offset [r b] (pixel-pos tile)))
  (defn top-right [tile] (offset [r t] (pixel-pos tile)))
  (defn right [tile] (offset [r m] (pixel-pos tile)))
  (defn left [tile] (offset [l m] (pixel-pos tile))))

(defn tile-at [x y]
  (tile (Math/floor (/ x 8)) (Math/floor (/ y 8))))

(defn tile-center? [x y]
  (and (= 4 (mod x 8)) (= 4 (mod y 8))))
