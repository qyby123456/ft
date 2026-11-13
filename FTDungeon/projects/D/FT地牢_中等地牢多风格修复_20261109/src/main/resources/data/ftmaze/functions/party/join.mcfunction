execute if score #run ftm_state matches 0 if dimension minecraft:overworld if entity @e[type=minecraft:marker,tag=ftmaze.lobby,distance=..8,limit=1] run tag @s add ftmaze.queued
execute if entity @s[tag=ftmaze.queued] run tellraw @s {"text":"已报名；请留在入口8格内等待开局快照。重复报名不会重复计数。","color":"green"}
