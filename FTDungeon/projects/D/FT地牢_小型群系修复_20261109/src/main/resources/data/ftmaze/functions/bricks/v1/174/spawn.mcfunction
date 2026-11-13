scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 398 31 31 minecraft:air if block 398 32 31 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 398 31 31 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 415 31 44 minecraft:air if block 415 32 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 415 31 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 413 31 25 minecraft:air if block 413 32 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 413 31 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 395 31 44 minecraft:air if block 395 32 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 395 31 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 405 31 39 minecraft:air if block 405 32 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 405 31 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 409 31 32 minecraft:air if block 409 32 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 409 31 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 402 31 25 minecraft:air if block 402 32 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 402 31 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 413 31 37 minecraft:air if block 413 32 37 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 413 31 37 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 399 31 31 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 399 31 31 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b174 ftm_brick 2
