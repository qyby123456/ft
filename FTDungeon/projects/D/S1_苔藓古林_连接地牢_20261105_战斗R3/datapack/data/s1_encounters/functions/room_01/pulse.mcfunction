execute if score #r1 s1_enc matches 0 if loaded 43 -56 19 run function s1_encounters:room_01/spawn
execute as @e[tag=s1_r1_u0] unless entity @s[x=30,y=-58,z=2,dx=24,dy=14,dz=24] run tp @s 43.5 -56 19.5
execute if score #r1 s1_enc matches 1 if loaded 43 -56 19 unless entity @e[tag=s1_room_1] run function s1_encounters:room_01/clear
execute if score #r1 s1_enc matches 1 if entity @e[tag=s1_boss,limit=1] run function s1_encounters:bossbar
