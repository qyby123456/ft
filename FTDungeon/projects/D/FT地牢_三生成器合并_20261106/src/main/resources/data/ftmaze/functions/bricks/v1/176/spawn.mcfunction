scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 484 31 30 minecraft:air if block 484 32 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 484 31 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 501 31 44 minecraft:air if block 501 32 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 501 31 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 499 31 25 minecraft:air if block 499 32 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 499 31 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 481 31 44 minecraft:air if block 481 32 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 481 31 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 491 31 38 minecraft:air if block 491 32 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 491 31 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 489 31 25 minecraft:air if block 489 32 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 489 31 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 494 31 30 minecraft:air if block 494 32 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 494 31 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 499 31 35 minecraft:air if block 499 32 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 499 31 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 485 31 30 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 485 31 30 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b176 ftm_brick 2
