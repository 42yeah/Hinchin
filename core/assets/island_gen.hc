store 1 # 1: y #
clone
store 2 # 2: x #
push 10
mul
push 10
load 1
mul
perlin # value #
clone store 5 # 5: perlin #
push 0.5
load 2 nml load 1 nml len push 1.0 mul nml # normalize(len((x,y))*1.0) #
sub clone store 6 # 6: comparator #
cmpf jg !place-sea
load 5 push 0.15 add
load 6
cmpf jg !place-sand
data 0 grass
check-room
    # check whether the room had been planted first #
    load 7 push 1 cmp je !plant-tree
        rnd push 0.98 cmpf jl !ret # if(rnd>0.02) #
        load 2 load 1 rnd push 0.1 mul rnd push 0.1 mul # xywh #
        room temple_room.hc
        push 1 store 7 # 7: room planted flag #
    j !ret
plant-tree
    load 2 load 1 getterrain 8 data 9 null push 8 push 9 cmpdata jne !ret # 8: is it floor? #
    rnd push 0.98 cmpf jl !ret # if(rnd>0.02) #
        data 3 tree push 3 load 2 load 1 plant plant(tree,x,y)
    j !ret
place-sea
    data 0 sea
    j !ret
place-sand
    data 0 sand
    j !ret
ret
push 0
end