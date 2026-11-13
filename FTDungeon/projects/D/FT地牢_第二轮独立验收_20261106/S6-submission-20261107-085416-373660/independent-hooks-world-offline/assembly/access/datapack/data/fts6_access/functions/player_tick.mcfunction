execute if score @s fts6_enter matches 1.. run function fts6_access:enter
execute if score @s fts6_exit matches 1.. run function fts6_access:exit
scoreboard players set @s fts6_enter 0
scoreboard players set @s fts6_exit 0
execute if entity @s[tag=fts6_authorized] at @e[type=minecraft:marker,tag=fts6_lobby,limit=1] if entity @s[distance=..2] run scoreboard players enable @s fts6_enter
execute at @e[type=minecraft:marker,tag=fts6_origin,limit=1] positioned ~2.5 ~4 ~36.5 if entity @s[distance=..2] run scoreboard players enable @s fts6_exit
