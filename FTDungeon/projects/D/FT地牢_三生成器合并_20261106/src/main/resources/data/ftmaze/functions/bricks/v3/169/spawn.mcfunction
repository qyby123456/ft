scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 398 211 112 minecraft:air if block 398 212 112 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 398 211 112 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 415 211 126 minecraft:air if block 415 212 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 415 211 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 413 211 107 minecraft:air if block 413 212 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 413 211 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 395 211 126 minecraft:air if block 395 212 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 395 211 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 405 211 120 minecraft:air if block 405 212 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 405 211 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 403 211 107 minecraft:air if block 403 212 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 403 211 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 408 211 112 minecraft:air if block 408 212 112 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 408 211 112 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 413 211 117 minecraft:air if block 413 212 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 413 211 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 399 211 112 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 399 211 112 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b169 ftm_brick 2
