scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 71 31 106 minecraft:air if block 71 32 106 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 71 31 106 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 76 31 94 minecraft:air if block 76 32 94 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 76 31 94 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 72 31 106 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 72 31 106 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b72 ftm_brick 2
