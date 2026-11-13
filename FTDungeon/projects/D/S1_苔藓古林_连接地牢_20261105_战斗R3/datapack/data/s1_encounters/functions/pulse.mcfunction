bossbar set s1_encounters:king visible false
bossbar set s1_encounters:king players
execute if score #enabled s1_enc matches 1 if entity @a[gamemode=!creative,gamemode=!spectator,x=64,y=-58,z=14,dx=22,dy=12,dz=20] run function s1_encounters:room_02/pulse
execute if score #enabled s1_enc matches 1 if entity @a[gamemode=!creative,gamemode=!spectator,x=30,y=-58,z=99,dx=18,dy=11,dz=16] run function s1_encounters:room_03/pulse
execute if score #enabled s1_enc matches 1 if entity @a[gamemode=!creative,gamemode=!spectator,x=60,y=-58,z=89,dx=30,dy=17,dz=34] run function s1_encounters:room_04/pulse
execute if score #enabled s1_enc matches 1 if entity @a[gamemode=!creative,gamemode=!spectator,x=68,y=-58,z=44,dx=14,dy=11,dz=36] run function s1_encounters:room_05/pulse
execute if score #enabled s1_enc matches 1 if entity @a[gamemode=!creative,gamemode=!spectator,x=0,y=-58,z=119,dx=24,dy=11,dz=24] run function s1_encounters:room_06/pulse
execute if score #enabled s1_enc matches 1 if entity @a[gamemode=!creative,gamemode=!spectator,x=110,y=-58,z=100,dx=34,dy=21,dz=34] run function s1_encounters:room_08/pulse
execute if score #enabled s1_enc matches 1 if entity @a[gamemode=!creative,gamemode=!spectator,x=106,y=-58,z=56,dx=28,dy=16,dz=28] run function s1_encounters:room_09/pulse
execute if score #enabled s1_enc matches 1 if entity @a[gamemode=!creative,gamemode=!spectator,x=30,y=-58,z=2,dx=24,dy=14,dz=24] run function s1_encounters:room_01/pulse
