store 1 #y
push 5
mul
push 5
load 1
mul
perlin
push 0.5
cmpf
jg 6
data 0 grass
j 4
data 0 sea
push 0
end