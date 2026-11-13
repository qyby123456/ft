scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 484 91 117 minecraft:air if block 484 92 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 484 91 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 501 91 108 minecraft:air if block 501 92 108 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 501 91 108 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 497 91 127 minecraft:air if block 497 92 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 497 91 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 488 91 107 minecraft:air if block 488 92 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 488 91 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 494 91 114 minecraft:air if block 494 92 114 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 494 91 114 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 481 91 126 minecraft:air if block 481 92 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 481 91 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 489 91 123 minecraft:air if block 489 92 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 489 91 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 500 91 119 minecraft:air if block 500 92 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 500 91 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 485 91 117 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 485 91 117 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b170 ftm_brick 2
