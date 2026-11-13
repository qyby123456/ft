bossbar set s1_encounters:king players @a[gamemode=!creative,gamemode=!spectator,x=42,y=-56,z=14,distance=..40]
execute store result bossbar s1_encounters:king max run attribute @e[tag=s1_boss,limit=1] minecraft:generic.max_health get 1
execute store result bossbar s1_encounters:king value run data get entity @e[tag=s1_boss,limit=1] Health 1
bossbar set s1_encounters:king visible true
