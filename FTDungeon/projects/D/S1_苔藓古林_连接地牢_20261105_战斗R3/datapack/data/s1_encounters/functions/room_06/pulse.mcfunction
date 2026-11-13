execute if score #r6 s1_enc matches 0 if loaded 13 -56 136 if loaded 17 -56 131 run function s1_encounters:room_06/spawn
execute as @e[tag=s1_r6_u0] unless entity @s[x=0,y=-58,z=119,dx=24,dy=11,dz=24] run tp @s 13.5 -56 136.5
execute as @e[tag=s1_r6_u1] unless entity @s[x=0,y=-58,z=119,dx=24,dy=11,dz=24] run tp @s 17.5 -56 131.5
execute if score #r6 s1_enc matches 1 if loaded 13 -56 136 if loaded 17 -56 131 unless entity @e[tag=s1_room_6] run function s1_encounters:room_06/clear
