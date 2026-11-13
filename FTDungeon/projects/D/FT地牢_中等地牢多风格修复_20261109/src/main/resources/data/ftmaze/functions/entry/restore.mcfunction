execute if score @s ftm_mode matches 0 run gamemode survival @s
execute if score @s ftm_mode matches 1 run gamemode creative @s
execute if score @s ftm_mode matches 2 run gamemode adventure @s
execute if score @s ftm_mode matches 3 run gamemode spectator @s
tag @s remove ftmaze.inside
