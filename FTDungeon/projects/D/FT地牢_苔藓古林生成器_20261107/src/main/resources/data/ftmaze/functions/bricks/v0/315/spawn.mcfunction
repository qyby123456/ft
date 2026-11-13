scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 498 211 198 minecraft:air if block 498 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 498 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 488 211 202 minecraft:air if block 488 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 488 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 503 211 202 minecraft:air if block 503 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 503 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 491 211 198 minecraft:air if block 491 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 491 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 495 211 202 minecraft:air if block 495 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 495 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 492 211 201 minecraft:air if block 492 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 492 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 494 211 199 minecraft:air if block 494 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 494 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 499 211 201 minecraft:air if block 499 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 499 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 499 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 499 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b315 ftm_brick 2
