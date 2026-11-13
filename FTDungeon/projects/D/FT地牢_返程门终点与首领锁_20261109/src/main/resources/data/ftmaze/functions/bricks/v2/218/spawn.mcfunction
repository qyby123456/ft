scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1401 211 117 minecraft:air if block 1401 212 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1401 211 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1418 211 107 minecraft:air if block 1418 212 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1418 211 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1414 211 127 minecraft:air if block 1414 212 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1414 211 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1398 211 107 minecraft:air if block 1398 212 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1398 211 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1398 211 127 minecraft:air if block 1398 212 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1398 211 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1408 211 110 minecraft:air if block 1408 212 110 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1408 211 110 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1415 211 116 minecraft:air if block 1415 212 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1415 211 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1406 211 123 minecraft:air if block 1406 212 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1406 211 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1402 211 117 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1402 211 117 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b218 ftm_brick 2
