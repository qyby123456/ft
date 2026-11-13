execute if score #r5 s1_enc matches 0 if loaded 75 -56 62 if loaded 75 -56 65 if loaded 75 -56 59 run function s1_encounters:room_05/spawn
execute as @e[tag=s1_r5_u0] unless entity @s[x=68,y=-58,z=44,dx=14,dy=11,dz=36] run tp @s 75.5 -56 62.5
execute as @e[tag=s1_r5_u1] unless entity @s[x=68,y=-58,z=44,dx=14,dy=11,dz=36] run tp @s 75.5 -56 65.5
execute as @e[tag=s1_r5_u2] unless entity @s[x=68,y=-58,z=44,dx=14,dy=11,dz=36] run tp @s 75.5 -56 59.5
execute if score #r5 s1_enc matches 1 if loaded 75 -56 62 if loaded 75 -56 65 if loaded 75 -56 59 unless entity @e[tag=s1_room_5] run function s1_encounters:room_05/clear
