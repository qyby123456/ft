scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 498 31 198 minecraft:air if block 498 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 498 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 488 31 202 minecraft:air if block 488 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 488 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 503 31 202 minecraft:air if block 503 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 503 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 491 31 198 minecraft:air if block 491 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 491 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 495 31 202 minecraft:air if block 495 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 495 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 492 31 201 minecraft:air if block 492 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 492 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 494 31 199 minecraft:air if block 494 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 494 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 499 31 201 minecraft:air if block 499 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 499 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 499 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 499 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b328 ftm_brick 2
