scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 398 91 71 minecraft:air if block 398 92 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 398 91 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 415 91 86 minecraft:air if block 415 92 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 415 91 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 414 91 66 minecraft:air if block 414 92 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 414 91 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 395 91 86 minecraft:air if block 395 92 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 395 91 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 405 91 80 minecraft:air if block 405 92 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 405 91 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 408 91 72 minecraft:air if block 408 92 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 408 91 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 403 91 66 minecraft:air if block 403 92 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 403 91 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 413 91 77 minecraft:air if block 413 92 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 413 91 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 399 91 71 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 399 91 71 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b171 ftm_brick 2
