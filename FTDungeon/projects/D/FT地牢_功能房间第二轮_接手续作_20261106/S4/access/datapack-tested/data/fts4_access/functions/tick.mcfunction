scoreboard players add @a fts4_inside 0
execute as @a[scores={fts4_enter=1..,fts4_inside=0}] at @s if dimension minecraft:overworld if entity @s[x=1014,y=259.5,z=1050,dx=5,dy=3,dz=5] run function fts4_access:enter
execute as @a[scores={fts4_exit=1..,fts4_inside=1}] at @s if dimension minecraft:overworld if entity @s[x=1024,y=256,z=1024,dx=414,dy=23,dz=66] run function fts4_access:return
execute as @a[scores={fts4_inside=1}] at @s unless dimension minecraft:overworld run scoreboard players set @s fts4_inside 0
execute as @a[scores={fts4_inside=1}] at @s if dimension minecraft:overworld unless entity @s[x=1024,y=256,z=1024,dx=414,dy=23,dz=66] run scoreboard players set @s fts4_inside 0
scoreboard players set @a fts4_enter 0
scoreboard players set @a fts4_exit 0
scoreboard players enable @a fts4_enter
scoreboard players enable @a fts4_exit
