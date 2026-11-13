scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 441 213 34 minecraft:air if block 441 214 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 441 213 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 458 213 24 minecraft:air if block 458 214 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 458 213 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 454 213 44 minecraft:air if block 454 214 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 454 213 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 438 213 24 minecraft:air if block 438 214 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 438 213 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 438 213 44 minecraft:air if block 438 214 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 438 213 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 448 213 27 minecraft:air if block 448 214 27 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 448 213 27 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 455 213 33 minecraft:air if block 455 214 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 455 213 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 446 213 40 minecraft:air if block 446 214 40 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 446 213 40 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 442 213 34 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 442 213 34 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b175 ftm_brick 2
