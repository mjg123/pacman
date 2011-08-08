(ns pacman.board)

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

(defn load []
  (let [board (atom {[-1 17] {:open nil} [28 17] {:open nil}})] ; this is lazy FP!
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