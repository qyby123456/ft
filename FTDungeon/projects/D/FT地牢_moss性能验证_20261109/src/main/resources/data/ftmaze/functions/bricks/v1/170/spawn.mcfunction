scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 484 31 117 minecraft:air if block 484 32 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 484 31 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 501 31 108 minecraft:air if block 501 32 108 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 501 31 108 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 497 31 127 minecraft:air if block 497 32 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 497 31 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 488 31 107 minecraft:air if block 488 32 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 488 31 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 494 31 114 minecraft:air if block 494 32 114 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 494 31 114 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 481 31 126 minecraft:air if block 481 32 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 481 31 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 489 31 123 minecraft:air if block 489 32 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 489 31 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 500 31 119 minecraft:air if block 500 32 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 500 31 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 485 31 117 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 485 31 117 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b170 ftm_brick 2
