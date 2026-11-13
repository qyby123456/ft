scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1401 31 117 minecraft:air if block 1401 32 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1401 31 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1418 31 107 minecraft:air if block 1418 32 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1418 31 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1414 31 127 minecraft:air if block 1414 32 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1414 31 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1398 31 107 minecraft:air if block 1398 32 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1398 31 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1398 31 127 minecraft:air if block 1398 32 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1398 31 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1408 31 110 minecraft:air if block 1408 32 110 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1408 31 110 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1415 31 116 minecraft:air if block 1415 32 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1415 31 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1406 31 123 minecraft:air if block 1406 32 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1406 31 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1402 31 117 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1402 31 117 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b218 ftm_brick 2
