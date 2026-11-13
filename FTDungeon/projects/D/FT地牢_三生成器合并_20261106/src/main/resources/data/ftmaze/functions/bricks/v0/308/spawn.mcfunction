scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 498 91 198 minecraft:air if block 498 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 498 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 488 91 202 minecraft:air if block 488 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 488 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 503 91 202 minecraft:air if block 503 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 503 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 491 91 198 minecraft:air if block 491 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 491 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 495 91 202 minecraft:air if block 495 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 495 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 492 91 201 minecraft:air if block 492 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 492 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 494 91 199 minecraft:air if block 494 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 494 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 499 91 201 minecraft:air if block 499 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 499 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 499 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 499 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b308 ftm_brick 2
