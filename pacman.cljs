(ns pacman
  (:require [goog.graphics :as gfx]
            [goog.dom :as dom]
            [goog.events :as events]
            [goog.events.KeyCodes :as key-codes]
            [goog.events.KeyHandler :as key-handler]
            [goog.Timer :as timer]))

;;;;;;;;;;;;;;;;;;;;

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

;;;;;;;;;;;;;;;;;;;;;

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

(defn read-board []
  (let [board (atom {[-1 17] {:open nil} [28 17] {:open nil}})]
    (doall (for [y (range (count board-str)) 
                 x (range (count (board-str 0)))]
      (let [sq (get-in board-str [y x])]
         (swap! board assoc [x y] 
           (cond
             (= sq \ ) {}
             (= sq \p) {:open nil :pellet nil}
             (= sq \o) {:open nil}
             (= sq \e) {:open nil :energy nil}
             :else nil)))))
    @board))
    
(def board (read-board))





;; Graphics stuff

(def pellet-color "#FFB9AF")
(def maze-color   "#00F")
(def fake-color   "#F0F")
(def pacman-fill (gfx/SolidFill. "#FF0"))

(defn tile [x y]
  [x y])

(defn pixel-pos [tile]
  (map #(* 8 %) tile))

(defn offset [off pos]
 (map #(+ %1 %2) off pos))

(let [l 0, m 4, r 7, t l, b r]
  (defn middle        [tile] (offset [m m] (pixel-pos tile)))
  (defn top-left      [tile] (offset [t l] (pixel-pos tile)))
  (defn bottom [tile] (offset [m b] (pixel-pos tile)))
  (defn bottom-left   [tile] (offset [l b] (pixel-pos tile)))
  (defn top    [tile] (offset [m t] (pixel-pos tile)))
  (defn bottom-right  [tile] (offset [r b] (pixel-pos tile)))
  (defn top-right     [tile] (offset [r t] (pixel-pos tile)))
  (defn right  [tile] (offset [r m] (pixel-pos tile)))
  (defn left   [tile] (offset [l m] (pixel-pos tile)))
)

(defn black-background [field]
  (.drawRect field 0 0 (.width field) (.height field) nil (gfx/SolidFill. "#000")))

(defn moveTo [path coords]
  (.moveTo path (first coords) (second coords)))

(defn lineTo [path coords]
  (.lineTo path (first coords) (second coords)))

(defn arcTo [path coords]
  (let [cur (.currentPoint_ path)]
    (.arcTo path (- (first coords) (first cur)) (- (second coords) (second cur)) -90 90)))

(defn larcTo [path coords]
  (let [cur (.currentPoint_ path)]
    (.arcTo path (- (first coords) (first cur)) (- (second coords) (second cur)) 180 -90)))

(defn island [x y r b]
  (let [path (gfx/Path.)]
    (-> path
      (moveTo (right (tile x y)))
      (lineTo (left (tile r y)))
      (arcTo (bottom (tile r y)))
      (lineTo (top (tile r b)))
      (arcTo (left (tile r b)))
      (lineTo (right (tile x b)))
      (arcTo (top (tile x b)))
      (lineTo (bottom (tile x y)))
      (arcTo (right (tile x y))))))

(defn t-shape [x y]
  (let [path (gfx/Path.)]
    (-> path
      (moveTo (right (tile x y)))
      (lineTo (left (tile (+ 7 x) y)))
      (arcTo (bottom (tile (+ 7 x) y)))
      (arcTo (left (tile (+ 7 x) (+ 1 y))))
      (lineTo (right (tile (+ 4 x) (+ 1 y))))
      (larcTo (bottom (tile (+ 4 x) (+ 1 y))))
      (lineTo (top (tile (+ 4 x) (+ 4 y))))
      (arcTo (left (tile (+ 4 x) (+ 4 y))))
      (arcTo (top (tile 13 (+ 4 y))))
      (lineTo (bottom (tile (+ 3 x) (+ 1 y))))
      (larcTo (left (tile (+ 3 x) (+ 1 y))))
      (lineTo (right (tile x (+ 1 y))))
      (arcTo (top (tile x (+ 1 y))))
      (arcTo (right (tile x y))))))


(defn draw-maze [field]
  (let [maze-stroke (gfx/Stroke. 1 maze-color)]
  (let [path (gfx/Path.)]
    (-> path
      (moveTo (top-left (tile 0 16)))
      (lineTo (top-left (tile 5 16)))
      (lineTo (bottom-left (tile 5 12)))
      (lineTo (bottom (tile 0 12)))
      (arcTo (left (tile 0 12)))
      (lineTo (left (tile 0 3)))
      (arcTo (top (tile 0 3)))
      (lineTo (top (tile 27 3)))
      (arcTo (right (tile 27 3)))
      (lineTo (right (tile 27 12)))
      (arcTo (bottom (tile 27 12)))
      (lineTo (bottom-right (tile 22 12)))
      (lineTo (top-right (tile 22 16)))
      (lineTo (top-right (tile 27 16))))
    (.drawPath field path maze-stroke))
  (let [path (gfx/Path.)]
    (-> path
      (moveTo (left (tile 0 16)))
      (lineTo (left (tile 5 16)))
      (larcTo (top (tile 5 16)))
      (lineTo (bottom (tile 5 12)))
      (larcTo (left (tile 5 12)))
      (lineTo (right (tile 0 12)))
      (arcTo (top (tile 0 12)))
      (lineTo (bottom (tile 0 3)))
      (arcTo (right (tile 0 3)))
      (lineTo (left (tile 13 3)))
      (arcTo (bottom (tile 13 3)))
      (lineTo (top (tile 13 7)))
      (larcTo (right (tile 13 7)))
      (larcTo (top (tile 14 7)))
      (lineTo (bottom (tile 14 3)))
      (arcTo (right (tile 14 3)))
      (lineTo (left (tile 27 3)))
      (arcTo (bottom (tile 27 3)))
      (lineTo (top (tile 27 12)))
      (arcTo (left (tile 27 12)))
      (lineTo (right (tile 22 12)))
      (larcTo (bottom (tile 22 12)))
      (lineTo (top (tile 22 16)))
      (larcTo (right (tile 22 16)))
      (lineTo (right (tile 27 16))))
    (.drawPath field path maze-stroke))

    (.drawPath field (island 2 5 5 7) maze-stroke)
    (.drawPath field (island 7 5 11 7) maze-stroke)
    (.drawPath field (island 16 5 20 7) maze-stroke)
    (.drawPath field (island 22 5 25 7) maze-stroke)
    (.drawPath field (island 2 9 5 10) maze-stroke)
    (.drawPath field (island 22 9 25 10) maze-stroke)
    (.drawPath field (island 7 18 8 22) maze-stroke)
    (.drawPath field (island 19 18 20 22) maze-stroke)
    (.drawPath field (island 7 24 11 25) maze-stroke)
    (.drawPath field (island 16 24 20 25) maze-stroke)

    (let [path (gfx/Path.)]
      (-> path
        (moveTo (middle (tile 10 15)))
        (lineTo (middle (tile 17 15)))
        (lineTo (middle (tile 17 19)))
        (lineTo (middle (tile 10 19)))
        (lineTo (middle (tile 10 15))))
      (.drawPath field path maze-stroke))

    (let [path (gfx/Path.)]
      (-> path
        (moveTo (bottom-right (tile 10 15)))
        (lineTo (bottom-left (tile 17 15)))
        (lineTo (top-left (tile 17 19)))
        (lineTo (top-right (tile 10 19)))
        (lineTo (bottom-right (tile 10 15))))
      (.drawPath field path maze-stroke))

    (.drawPath field (t-shape 10 9) maze-stroke)
    (.drawPath field (t-shape 10 21) maze-stroke)
    (.drawPath field (t-shape 10 27) maze-stroke)

    (let [path (gfx/Path.)]
      (-> path
        (moveTo (bottom (tile 7 9)))
        (arcTo (right (tile 7 9)))
        (lineTo (left (tile 8 9)))
        (arcTo (bottom (tile 8 9)))
        (lineTo (top (tile 8 12)))
        (larcTo (right (tile 8 12)))
        (lineTo (left (tile 11 12)))
        (arcTo (bottom (tile 11 12)))
        (lineTo (top (tile 11 13)))
        (arcTo (left (tile 11 13)))
        (lineTo (right (tile 8 13)))
        (larcTo (bottom (tile 8 13)))
        (lineTo (top (tile 8 16)))
        (arcTo (left (tile 8 16)))
        (lineTo (right (tile 7 16)))
        (arcTo (top (tile 7 16)))
        (lineTo (bottom (tile 7 9))))
      (.drawPath field path maze-stroke))

    (let [path (gfx/Path.)]
      (-> path
        (moveTo (bottom (tile 19 9)))
        (arcTo (right (tile 19 9)))
        (lineTo (left (tile 20 9)))
        (arcTo (bottom (tile 20 9)))
        (lineTo (top (tile 20 16)))
        (arcTo (left (tile 20 16)))
        (lineTo (right (tile 19 16)))
        (arcTo (top (tile 19 16)))
        (lineTo (bottom (tile 19 13)))
        (larcTo (left (tile 19 13)))
        (lineTo (right (tile 16 13)))
        (arcTo (top (tile 16 13)))
        (lineTo (bottom (tile 16 12)))
        (arcTo (right (tile 16 12)))
        (lineTo (left (tile 19 12)))
        (larcTo (top (tile 19 12)))
        (lineTo (bottom (tile 19 9))))
      (.drawPath field path maze-stroke))

    (let [path (gfx/Path.)]
      (-> path
        (moveTo (bottom (tile 2 24)))
        (arcTo (right (tile 2 24)))
        (lineTo (left (tile 5 24)))
        (arcTo (bottom (tile 5 24)))
        (lineTo (top (tile 5 28)))
        (arcTo (left (tile 5 28)))
        (lineTo (right (tile 4 28)))
        (arcTo (top (tile 4 28)))
        (lineTo (bottom (tile 4 25)))
        (larcTo (left (tile 4 25)))
        (lineTo (right (tile 2 25)))
        (arcTo (top (tile 2 25)))
        (lineTo (bottom (tile 2 24))))
      (.drawPath field path maze-stroke))

    (let [path (gfx/Path.)]
      (-> path
        (moveTo (bottom (tile 22 24)))
        (arcTo (right (tile 22 24)))
        (lineTo (left (tile 25 24)))
        (arcTo (bottom (tile 25 24)))
        (lineTo (top (tile 25 25)))
        (arcTo (left (tile 25 25)))
        (lineTo (right (tile 23 25)))
        (larcTo (bottom (tile 23 25)))
        (lineTo (top (tile 23 28)))
        (arcTo (left (tile 23 28)))
        (lineTo (right (tile 22 28)))
        (arcTo (top (tile 22 28)))
        (lineTo (bottom (tile 22 24))))
      (.drawPath field path maze-stroke))


))
   
 
(defn draw-pellet [field tile]
  (let [pellet-stroke (gfx/Stroke. 1 pellet-color)
        pellet-fill   (gfx/SolidFill. pellet-color)
        [x y] (middle tile)]
    (.drawCircle field x y 1 pellet-stroke pellet-fill)))

(defn draw-energy [field tile]
  (let [pellet-stroke (gfx/Stroke. 1 pellet-color)
        pellet-fill   (gfx/SolidFill. pellet-color)
        [x y] (middle tile)]
    (.drawCircle field x y 3 pellet-stroke pellet-fill)))

(defn draw-board [field board]
  (doall (map 
    #(let [[[x y] pt] %]
       (cond
         (contains? pt :pellet) (draw-pellet field (tile x y))
         (contains? pt :energy) (draw-energy field (tile x y))))
    board)))

(defn draw-pacman [field pman]
  (let [[x y] (pman :pos)
        elem (.drawCircle field x y 6 nil pacman-fill)]
    (assoc pman :element elem)))

(def deltas {:west [-1 0] :east [1 0] :north [0 -1] :south [0 1] :none [0 0]})
(def pacman-start {:pos (left (tile 14 26)) :face :west})

(def keypress (atom nil))

(defn tile-at [x y]
  (tile (Math/floor (/ x 8)) (Math/floor (/ y 8))))

(defn use-key []
  (reset! keypress nil))

(defn tile-center? [x y]
  (and (= 4 (mod x 8)) (= 4 (mod y 8))))

(defn get-new-face [x y kp old-face board]
  (let [[tx ty] (tile-at x y)]
    (if (tile-center? x y)
      (cond
        (and (= :north kp) (contains? (board [tx (- ty 1)]) :open)) (do (use-key) kp)
        (and (= :south kp) (contains? (board [tx (+ ty 1)]) :open)) (do (use-key) kp)
        (and (= :east  kp) (contains? (board [(+ tx 1) ty]) :open)) (do (use-key) kp)
        (and (= :west  kp) (contains? (board [(- tx 1) ty]) :open)) (do (use-key) kp)
        (and (= :north old-face) (contains? (board [tx (- ty 1)]) :open)) old-face
        (and (= :south old-face) (contains? (board [tx (+ ty 1)]) :open)) old-face
        (and (= :east  old-face) (contains? (board [(+ tx 1) ty]) :open)) old-face
        (and (= :west  old-face) (contains? (board [(- tx 1) ty]) :open)) old-face
        :else :none)
      old-face)))

(defn update-pacman [old]
  (let [[x y] (old :pos)
        [tx ty] (tile-at x y)
        new-face (get-new-face x y @keypress (old :face) board)
        [dx dy] (deltas new-face)
        new-pos [(mod (+ 224 x dx) 224) (+ y dy)]]
    (debug (pr-str [tx ty]))
    (.setCenter (old :element) (first new-pos) (second new-pos))
    (assoc old 
      :face new-face
      :pos new-pos)))

(defn gameloop [state]
  (let [n-pacman (update-pacman (state :pacman))]
    (timer/callOnce #(gameloop {:pacman n-pacman}) 17)))

(defn create-playfield []
  (let [field (gfx/createGraphics 224 288)]

    (read-board)

    (black-background field)
    (draw-maze field)

    (draw-board field board)

    (let [pacman (draw-pacman field pacman-start)]
      (.render field (dom/getElement "playfield"))
      (gameloop {:pacman pacman}))))


(create-playfield)

(defn handle-key [key]
  (let [code (.keyCode key)]
    (cond
      (= code key-codes/UP) (reset! keypress :north)
      (= code key-codes/DOWN) (reset! keypress :south)
      (= code key-codes/LEFT) (reset! keypress :west)
      (= code key-codes/RIGHT) (reset! keypress :east))))

(events/listen (events/KeyHandler. (js* "document"))
               "key"
               handle-key)

