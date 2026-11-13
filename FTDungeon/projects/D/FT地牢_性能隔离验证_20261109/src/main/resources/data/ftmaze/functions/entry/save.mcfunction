scoreboard players set @s ftm_mode 0
execute if entity @s[gamemode=creative] run scoreboard players set @s ftm_mode 1
execute if entity @s[gamemode=adventure] run scoreboard players set @s ftm_mode 2
execute if entity @s[gamemode=spectator] run scoreboard players set @s ftm_mode 3
tag @s add ftmaze.inside
gamemode adventure @s
execute in ftmaze:depths run tp @s -15.5 211 88.5 -90 0
tellraw @s {"text":"小房间为主路；体系房与主题区从支路接入。每层两守卫开启下层，最终Boss胜利才开折返门。","color":"aqua"}
