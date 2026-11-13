scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 398 31 71 minecraft:air if block 398 32 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 398 31 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 415 31 86 minecraft:air if block 415 32 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 415 31 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 414 31 66 minecraft:air if block 414 32 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 414 31 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 395 31 86 minecraft:air if block 395 32 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 395 31 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 405 31 80 minecraft:air if block 405 32 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 405 31 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 408 31 72 minecraft:air if block 408 32 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 408 31 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 403 31 66 minecraft:air if block 403 32 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 403 31 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 413 31 77 minecraft:air if block 413 32 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 413 31 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 399 31 71 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 399 31 71 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b171 ftm_brick 2
