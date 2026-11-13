scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 398 91 31 minecraft:air if block 398 92 31 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 398 91 31 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 415 91 44 minecraft:air if block 415 92 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 415 91 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 413 91 25 minecraft:air if block 413 92 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 413 91 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 395 91 44 minecraft:air if block 395 92 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 395 91 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 405 91 39 minecraft:air if block 405 92 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 405 91 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 409 91 32 minecraft:air if block 409 92 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 409 91 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 402 91 25 minecraft:air if block 402 92 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 402 91 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 413 91 37 minecraft:air if block 413 92 37 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 413 91 37 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 399 91 31 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 399 91 31 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b174 ftm_brick 2
