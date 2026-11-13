scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 398 151 24 minecraft:air if block 398 152 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 398 151 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 415 151 38 minecraft:air if block 415 152 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 415 151 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 413 151 18 minecraft:air if block 413 152 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 413 151 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 395 151 38 minecraft:air if block 395 152 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 395 151 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 405 151 32 minecraft:air if block 405 152 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 405 151 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 409 151 25 minecraft:air if block 409 152 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 409 151 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 402 151 18 minecraft:air if block 402 152 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 402 151 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 413 151 30 minecraft:air if block 413 152 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 413 151 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 399 151 24 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 399 151 24 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b174 ftm_brick 2
