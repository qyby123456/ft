scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 484 211 117 minecraft:air if block 484 212 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 484 211 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 501 211 108 minecraft:air if block 501 212 108 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 501 211 108 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 497 211 127 minecraft:air if block 497 212 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 497 211 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 488 211 107 minecraft:air if block 488 212 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 488 211 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 494 211 114 minecraft:air if block 494 212 114 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 494 211 114 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 481 211 126 minecraft:air if block 481 212 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 481 211 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 489 211 123 minecraft:air if block 489 212 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 489 211 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 500 211 119 minecraft:air if block 500 212 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 500 211 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 485 211 117 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 485 211 117 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b170 ftm_brick 2
