Pac-Man in-browser using clojurescript

todo
 - move :ghost-mode into [:ghosts :mode]
 - pacman stops for 3 frames when he eats energy (not 1 like pellets)
 - pacman dies if eaten + lives etc
 - fast cornering
 - ghosts start in the ghost house
 - energy -> frightened ghosts, scatter timer stops, pacman can eat them

 - hi-score table in local storage
 - testing framework
 - compiler warning about undeclared vars
 - html config: show-ghost-targets, slow-x10, invincible

 - FF 3.6 - "too much recursion" error
 - FF 4,5 - SVG error caused by -ve radii in arcTo commands

done
 - ghosts reverse direction on mode-change 13/8
 - proper timings for scattering etc 13/8
 - ghosts can't turn up/down towards their house 13/8
 - speeds of pacman & ghost adjusted, pacman pauses 1-frame after eating a pellet. 11/8
 - ghosts slow through teleporter tunnel 11/8
 - score 11/8
 - animated pacman 11/8
 - proper ordering for ghost choices when distances are equal (up, down, left, right) 11/8
 - ghost eyes should be tall and thin, not circles 11/8
 - different targetting for each ghost 10/8
 - better-timed game loop 10/8
 - ghostal eyes 10/8
 - alternative key mappings 10/8
 - ghosts 9/8
 - finish the jeffing maze! 9/8
 - should be able to reverse direction without waiting for tile-center 8/8
 - gobbling pellets and energy 8/8
 - pacman, movement, maze, key-control, etc 7/8

 