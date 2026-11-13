scoreboard players set #brickok ftm_state 0
execute if score #brickok ftm_state matches 1.. if block 52 91 63 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 52 91 63 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b128 ftm_brick 2
