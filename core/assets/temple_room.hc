store 1 # wall state #
store 3 store 2 # x, y #
load 1 push -1 cmp je !place-door-or-wall
load 1 push 0 cmp je !place-wall
# check stair planted state #
load 4 push 1 cmp je !place-floor
place-downstairs
    data 5 downstairs push 5 load 2 load 3 plant
    push 1 store 4
place-floor
    data 0 floor
    j !ret
place-door-or-wall
    rnd push 0.1 cmpf jge !place-wall
    data 0 door
    j !ret
place-wall
    data 0 wall
ret
    push 0
end