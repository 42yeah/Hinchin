push 1
cmp je !place-wall
    data 0 floor
    j !ret
place-wall
    data 0 wall
ret
push 0
end