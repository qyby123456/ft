scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 398 31 112 minecraft:air if block 398 32 112 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 398 31 112 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 415 31 126 minecraft:air if block 415 32 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 415 31 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 413 31 107 minecraft:air if block 413 32 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 413 31 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 395 31 126 minecraft:air if block 395 32 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 395 31 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 405 31 120 minecraft:air if block 405 32 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 405 31 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 403 31 107 minecraft:air if block 403 32 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 403 31 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 408 31 112 minecraft:air if block 408 32 112 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 408 31 112 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 413 31 117 minecraft:air if block 413 32 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 413 31 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 399 31 112 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 399 31 112 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b169 ftm_brick 2
