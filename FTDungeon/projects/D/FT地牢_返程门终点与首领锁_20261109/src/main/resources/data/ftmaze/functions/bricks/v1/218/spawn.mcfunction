scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1401 91 117 minecraft:air if block 1401 92 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1401 91 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1418 91 107 minecraft:air if block 1418 92 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1418 91 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1414 91 127 minecraft:air if block 1414 92 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1414 91 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1398 91 107 minecraft:air if block 1398 92 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1398 91 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1398 91 127 minecraft:air if block 1398 92 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1398 91 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1408 91 110 minecraft:air if block 1408 92 110 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1408 91 110 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1415 91 116 minecraft:air if block 1415 92 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1415 91 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1406 91 123 minecraft:air if block 1406 92 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1406 91 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1402 91 117 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1402 91 117 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b218 ftm_brick 2
