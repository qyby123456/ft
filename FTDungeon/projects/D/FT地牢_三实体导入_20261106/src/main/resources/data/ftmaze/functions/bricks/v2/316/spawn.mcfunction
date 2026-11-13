scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 498 151 198 minecraft:air if block 498 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 498 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 488 151 202 minecraft:air if block 488 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 488 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 503 151 202 minecraft:air if block 503 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 503 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 491 151 198 minecraft:air if block 491 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 491 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 495 151 202 minecraft:air if block 495 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 495 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 492 151 201 minecraft:air if block 492 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 492 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 494 151 199 minecraft:air if block 494 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 494 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 499 151 201 minecraft:air if block 499 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 499 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 499 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 499 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b316 ftm_brick 2
