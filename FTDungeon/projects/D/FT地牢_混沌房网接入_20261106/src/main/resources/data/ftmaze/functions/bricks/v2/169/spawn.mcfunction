scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 398 91 112 minecraft:air if block 398 92 112 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 398 91 112 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 415 91 126 minecraft:air if block 415 92 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 415 91 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 413 91 107 minecraft:air if block 413 92 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 413 91 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 395 91 126 minecraft:air if block 395 92 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 395 91 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 405 91 120 minecraft:air if block 405 92 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 405 91 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 403 91 107 minecraft:air if block 403 92 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 403 91 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 408 91 112 minecraft:air if block 408 92 112 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 408 91 112 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 413 91 117 minecraft:air if block 413 92 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 413 91 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 399 91 112 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 399 91 112 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b169 ftm_brick 2
