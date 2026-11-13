scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 398 211 71 minecraft:air if block 398 212 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 398 211 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 415 211 86 minecraft:air if block 415 212 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 415 211 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 414 211 66 minecraft:air if block 414 212 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 414 211 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 395 211 86 minecraft:air if block 395 212 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 395 211 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 405 211 80 minecraft:air if block 405 212 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 405 211 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 408 211 72 minecraft:air if block 408 212 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 408 211 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 403 211 66 minecraft:air if block 403 212 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 403 211 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 413 211 77 minecraft:air if block 413 212 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 413 211 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 399 211 71 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 399 211 71 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b171 ftm_brick 2
