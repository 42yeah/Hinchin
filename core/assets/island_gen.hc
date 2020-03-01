store 1 #y
clone
store 2 #x
push 10
mul
push 10
load 1
mul
perlin #value
push 0.5
load 2 nml load 1 nml len push 1.0 mul nml #normalize(len((x,y))*1.0)
sub
cmpf
jg !place-sea
data 0 grass
load 4 push 0 cmp jne !plant-tree
    data 3 downstairs push 3 load 2 load 1 plant
    push 1 store 4
j !ret
plant-tree
rnd push 0.98 cmpf jl !ret #if(rnd>0.5)
data 3 tree push 3 load 2 load 1 plant #plant(tree,x,y)
j !ret
place-sea data 0 sea
ret
push 0
end