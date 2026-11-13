scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 484 31 72 minecraft:air if block 484 32 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 484 31 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 501 31 86 minecraft:air if block 501 32 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 501 31 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 499 31 66 minecraft:air if block 499 32 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 499 31 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 481 31 86 minecraft:air if block 481 32 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 481 31 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 491 31 80 minecraft:air if block 491 32 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 491 31 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 495 31 73 minecraft:air if block 495 32 73 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 495 31 73 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 488 31 66 minecraft:air if block 488 32 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 488 31 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 499 31 78 minecraft:air if block 499 32 78 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 499 31 78 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 485 31 72 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 485 31 72 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b173 ftm_brick 2
