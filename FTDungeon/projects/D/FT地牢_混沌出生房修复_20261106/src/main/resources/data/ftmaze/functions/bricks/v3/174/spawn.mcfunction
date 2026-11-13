scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 398 211 31 minecraft:air if block 398 212 31 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 398 211 31 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 415 211 44 minecraft:air if block 415 212 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 415 211 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 413 211 25 minecraft:air if block 413 212 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 413 211 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 395 211 44 minecraft:air if block 395 212 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 395 211 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 405 211 39 minecraft:air if block 405 212 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 405 211 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 409 211 32 minecraft:air if block 409 212 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 409 211 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 402 211 25 minecraft:air if block 402 212 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 402 211 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 413 211 37 minecraft:air if block 413 212 37 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 413 211 37 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 399 211 31 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 399 211 31 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b174 ftm_brick 2
