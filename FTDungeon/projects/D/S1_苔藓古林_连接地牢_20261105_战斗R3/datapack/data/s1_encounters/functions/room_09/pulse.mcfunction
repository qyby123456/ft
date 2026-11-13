execute if score #r9 s1_enc matches 0 if loaded 120 -54 70 if loaded 122 -55 72 run function s1_encounters:room_09/spawn
execute as @e[tag=s1_r9_u0] unless entity @s[x=106,y=-58,z=56,dx=28,dy=16,dz=28] run tp @s 120.5 -54 70.5
execute as @e[tag=s1_r9_u1] unless entity @s[x=106,y=-58,z=56,dx=28,dy=16,dz=28] run tp @s 122.5 -55 72.5
execute if score #r9 s1_enc matches 1 if loaded 120 -54 70 if loaded 122 -55 72 unless entity @e[tag=s1_room_9] run function s1_encounters:room_09/clear
