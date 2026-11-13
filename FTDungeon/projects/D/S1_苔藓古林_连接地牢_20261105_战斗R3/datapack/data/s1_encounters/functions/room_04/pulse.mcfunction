execute if score #r4 s1_enc matches 0 if loaded 76 -56 106 if loaded 73 -56 106 if loaded 75 -56 109 run function s1_encounters:room_04/spawn
execute as @e[tag=s1_r4_u0] unless entity @s[x=60,y=-58,z=89,dx=30,dy=17,dz=34] run tp @s 76.5 -56 106.5
execute as @e[tag=s1_r4_u1] unless entity @s[x=60,y=-58,z=89,dx=30,dy=17,dz=34] run tp @s 73.5 -56 106.5
execute as @e[tag=s1_r4_u2] unless entity @s[x=60,y=-58,z=89,dx=30,dy=17,dz=34] run tp @s 75.5 -56 109.5
execute if score #r4 s1_enc matches 1 if loaded 76 -56 106 if loaded 73 -56 106 if loaded 75 -56 109 unless entity @e[tag=s1_room_4] run function s1_encounters:room_04/clear
