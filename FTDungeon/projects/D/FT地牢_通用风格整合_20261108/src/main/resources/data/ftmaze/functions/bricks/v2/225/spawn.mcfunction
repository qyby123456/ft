scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1401 213 34 minecraft:air if block 1401 214 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1401 213 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1418 213 24 minecraft:air if block 1418 214 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1418 213 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1414 213 44 minecraft:air if block 1414 214 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1414 213 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1398 213 24 minecraft:air if block 1398 214 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1398 213 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1398 213 44 minecraft:air if block 1398 214 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1398 213 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1408 213 27 minecraft:air if block 1408 214 27 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1408 213 27 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1415 213 33 minecraft:air if block 1415 214 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1415 213 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1406 213 40 minecraft:air if block 1406 214 40 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1406 213 40 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1402 213 34 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1402 213 34 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b225 ftm_brick 2
