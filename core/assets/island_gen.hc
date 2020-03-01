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
jg 6
data 0 grass
j 4
data 0 sea
push 0
end