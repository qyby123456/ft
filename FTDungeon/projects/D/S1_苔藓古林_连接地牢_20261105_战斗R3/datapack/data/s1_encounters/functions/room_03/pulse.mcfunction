execute if score #r3 s1_enc matches 0 if loaded 37 -56 108 if loaded 41 -56 110 run function s1_encounters:room_03/spawn
execute as @e[tag=s1_r3_u0] unless entity @s[x=30,y=-58,z=99,dx=18,dy=11,dz=16] run tp @s 37.5 -56 108.5
execute as @e[tag=s1_r3_u1] unless entity @s[x=30,y=-58,z=99,dx=18,dy=11,dz=16] run tp @s 41.5 -56 110.5
execute if score #r3 s1_enc matches 1 if loaded 37 -56 108 if loaded 41 -56 110 unless entity @e[tag=s1_room_3] run function s1_encounters:room_03/clear
