execute if score #r8 s1_enc matches 0 if loaded 128 -56 119 if loaded 125 -56 119 if loaded 129 -56 115 run function s1_encounters:room_08/spawn
execute as @e[tag=s1_r8_u0] unless entity @s[x=110,y=-58,z=100,dx=34,dy=21,dz=34] run tp @s 128.5 -56 119.5
execute as @e[tag=s1_r8_u1] unless entity @s[x=110,y=-58,z=100,dx=34,dy=21,dz=34] run tp @s 125.5 -56 119.5
execute as @e[tag=s1_r8_u2] unless entity @s[x=110,y=-58,z=100,dx=34,dy=21,dz=34] run tp @s 129.5 -56 115.5
execute if score #r8 s1_enc matches 1 if loaded 128 -56 119 if loaded 125 -56 119 if loaded 129 -56 115 unless entity @e[tag=s1_room_8] run function s1_encounters:room_08/clear
