scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1401 93 34 minecraft:air if block 1401 94 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1401 93 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1418 93 24 minecraft:air if block 1418 94 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1418 93 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1414 93 44 minecraft:air if block 1414 94 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1414 93 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1398 93 24 minecraft:air if block 1398 94 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1398 93 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1398 93 44 minecraft:air if block 1398 94 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1398 93 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1408 93 27 minecraft:air if block 1408 94 27 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1408 93 27 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1415 93 33 minecraft:air if block 1415 94 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1415 93 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1406 93 40 minecraft:air if block 1406 94 40 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1406 93 40 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1402 93 34 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1402 93 34 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b225 ftm_brick 2
