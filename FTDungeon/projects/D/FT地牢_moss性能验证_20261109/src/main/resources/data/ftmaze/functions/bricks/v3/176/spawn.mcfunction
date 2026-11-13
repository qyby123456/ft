scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 484 211 30 minecraft:air if block 484 212 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 484 211 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 501 211 44 minecraft:air if block 501 212 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 501 211 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 499 211 25 minecraft:air if block 499 212 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 499 211 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 481 211 44 minecraft:air if block 481 212 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 481 211 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 491 211 38 minecraft:air if block 491 212 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 491 211 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 489 211 25 minecraft:air if block 489 212 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 489 211 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 494 211 30 minecraft:air if block 494 212 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 494 211 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 499 211 35 minecraft:air if block 499 212 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 499 211 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 485 211 30 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 485 211 30 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b176 ftm_brick 2
