scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 484 91 30 minecraft:air if block 484 92 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 484 91 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 501 91 44 minecraft:air if block 501 92 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 501 91 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 499 91 25 minecraft:air if block 499 92 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 499 91 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 481 91 44 minecraft:air if block 481 92 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 481 91 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 491 91 38 minecraft:air if block 491 92 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 491 91 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 489 91 25 minecraft:air if block 489 92 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 489 91 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 494 91 30 minecraft:air if block 494 92 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 494 91 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 499 91 35 minecraft:air if block 499 92 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 499 91 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 485 91 30 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 485 91 30 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b176 ftm_brick 2
