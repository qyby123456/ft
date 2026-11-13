execute as @a[tag=ftmaze.inside] at @s unless dimension ftmaze:depths run function ftmaze:entry/restore
execute if score #layout ftm_state matches 1 run function ftmaze:v0/tick
execute if score #layout ftm_state matches 2 run function ftmaze:v1/tick
execute if score #layout ftm_state matches 3 run function ftmaze:v2/tick
execute if score #layout ftm_state matches 4 run function ftmaze:v3/tick
execute if score #g0 ftm_state matches 2 if score #g1 ftm_state matches 2 unless score #floor0 ftm_state matches 1 in ftmaze:depths if loaded 269 211 221 run function ftmaze:progress/floor_0
execute if score #g2 ftm_state matches 2 if score #g3 ftm_state matches 2 unless score #floor1 ftm_state matches 1 in ftmaze:depths if loaded 289 151 221 run function ftmaze:progress/floor_1
execute if score #g4 ftm_state matches 2 if score #g5 ftm_state matches 2 unless score #floor2 ftm_state matches 1 in ftmaze:depths if loaded 269 91 221 run function ftmaze:progress/floor_2
execute if score #run ftm_state matches 2 if score #g0 ftm_state matches 2 if score #g1 ftm_state matches 2 if score #g2 ftm_state matches 2 if score #g3 ftm_state matches 2 if score #g4 ftm_state matches 2 if score #g5 ftm_state matches 2 in ftmaze:depths if entity @a[tag=ftmaze.inside,x=130,y=30,z=260,dx=20,dy=10,dz=20] run function ftmaze:boss/start
execute if score #run ftm_state matches 3 in ftmaze:depths run function ftmaze:boss/tick
execute if score #run ftm_state matches 4 in ftmaze:depths as @a[tag=ftmaze.inside,x=139,y=31,z=285,dx=2,dy=3,dz=9] at @s run function ftmaze:entry/return
execute if score #run ftm_state matches 2..3 in ftmaze:depths run function ftmaze:bricks/tick
