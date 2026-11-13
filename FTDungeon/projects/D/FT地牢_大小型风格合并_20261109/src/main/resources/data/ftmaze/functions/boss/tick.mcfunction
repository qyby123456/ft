bossbar set ftmaze:final visible true
bossbar set ftmaze:final players @a[tag=ftmaze.inside]
execute store result bossbar ftmaze:final value run data get entity @e[tag=ftmaze.final,limit=1] Health 1
execute as @e[tag=ftmaze.final] unless entity @s[x=127,y=31,z=257,dx=26,dy=11,dz=26] run tp @s 140 31 270
