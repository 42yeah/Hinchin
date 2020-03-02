push 1
   cmp je !place-wall
       data 0 floor
       j !ret
   place-wall
       rnd push 0.1 cmpf jl !place-door
       data 0 wall
       j !ret
   place-door
       data 0 door
   ret
   push 0
   end