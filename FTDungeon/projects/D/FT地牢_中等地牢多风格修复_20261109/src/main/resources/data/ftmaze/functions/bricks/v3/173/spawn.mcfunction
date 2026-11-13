scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 484 211 72 minecraft:air if block 484 212 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 484 211 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 501 211 86 minecraft:air if block 501 212 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 501 211 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 499 211 66 minecraft:air if block 499 212 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 499 211 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 481 211 86 minecraft:air if block 481 212 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 481 211 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 491 211 80 minecraft:air if block 491 212 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 491 211 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 495 211 73 minecraft:air if block 495 212 73 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 495 211 73 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 488 211 66 minecraft:air if block 488 212 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 488 211 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 499 211 78 minecraft:air if block 499 212 78 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 499 211 78 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 485 211 72 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 485 211 72 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b173 ftm_brick 2
