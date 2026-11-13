execute if score #run ftm_state matches 2..3 unless score #g0 ftm_state matches 1.. in ftmaze:depths if entity @a[tag=ftmaze.inside,x=72,y=210,z=58,dx=16,dy=10,dz=16] run function ftmaze:v0/guard_0
execute in ftmaze:depths positioned 80 211 66 as @e[tag=ftmaze.guard0,distance=12..] run tp @s 80 211 66
execute if score #run ftm_state matches 2..3 unless score #g1 ftm_state matches 1.. in ftmaze:depths if entity @a[tag=ftmaze.inside,x=192,y=210,z=53,dx=16,dy=10,dz=16] run function ftmaze:v0/guard_1
execute in ftmaze:depths positioned 200 211 61 as @e[tag=ftmaze.guard1,distance=12..] run tp @s 200 211 61
execute if score #run ftm_state matches 2..3 unless score #g2 ftm_state matches 1.. in ftmaze:depths if entity @a[tag=ftmaze.inside,x=72,y=150,z=55,dx=16,dy=10,dz=16] run function ftmaze:v0/guard_2
execute in ftmaze:depths positioned 80 151 63 as @e[tag=ftmaze.guard2,distance=12..] run tp @s 80 151 63
execute if score #run ftm_state matches 2..3 unless score #g3 ftm_state matches 1.. in ftmaze:depths if entity @a[tag=ftmaze.inside,x=192,y=150,z=58,dx=16,dy=10,dz=16] run function ftmaze:v0/guard_3
execute in ftmaze:depths positioned 200 151 66 as @e[tag=ftmaze.guard3,distance=12..] run tp @s 200 151 66
execute if score #run ftm_state matches 2..3 unless score #g4 ftm_state matches 1.. in ftmaze:depths if entity @a[tag=ftmaze.inside,x=72,y=90,z=58,dx=16,dy=10,dz=16] run function ftmaze:v0/guard_4
execute in ftmaze:depths positioned 80 91 66 as @e[tag=ftmaze.guard4,distance=12..] run tp @s 80 91 66
execute if score #run ftm_state matches 2..3 unless score #g5 ftm_state matches 1.. in ftmaze:depths if entity @a[tag=ftmaze.inside,x=192,y=90,z=61,dx=16,dy=10,dz=16] run function ftmaze:v0/guard_5
execute in ftmaze:depths positioned 200 91 69 as @e[tag=ftmaze.guard5,distance=12..] run tp @s 200 91 69
