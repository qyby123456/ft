scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 484 91 72 minecraft:air if block 484 92 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 484 91 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 501 91 86 minecraft:air if block 501 92 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 501 91 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 499 91 66 minecraft:air if block 499 92 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 499 91 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 481 91 86 minecraft:air if block 481 92 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 481 91 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 491 91 80 minecraft:air if block 491 92 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 491 91 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 495 91 73 minecraft:air if block 495 92 73 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 495 91 73 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 488 91 66 minecraft:air if block 488 92 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 488 91 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 499 91 78 minecraft:air if block 499 92 78 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 499 91 78 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 485 91 72 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 485 91 72 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b173 ftm_brick 2
