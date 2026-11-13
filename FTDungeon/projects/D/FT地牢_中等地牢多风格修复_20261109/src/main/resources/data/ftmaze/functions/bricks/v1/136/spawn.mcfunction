scoreboard players set #brickok ftm_state 0
execute if score #brickok ftm_state matches 1.. if block 172 91 66 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 172 91 66 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b136 ftm_brick 2
