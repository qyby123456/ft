execute if score #r2 s1_enc matches 0 if loaded 75 -56 27 if loaded 78 -56 25 run function s1_encounters:room_02/spawn
execute as @e[tag=s1_r2_u0] unless entity @s[x=64,y=-58,z=14,dx=22,dy=12,dz=20] run tp @s 75.5 -56 27.5
execute as @e[tag=s1_r2_u1] unless entity @s[x=64,y=-58,z=14,dx=22,dy=12,dz=20] run tp @s 78.5 -56 25.5
execute if score #r2 s1_enc matches 1 if loaded 75 -56 27 if loaded 78 -56 25 unless entity @e[tag=s1_room_2] run function s1_encounters:room_02/clear
