scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1413 151 198 minecraft:air if block 1413 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1413 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1403 151 202 minecraft:air if block 1403 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1403 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1418 151 202 minecraft:air if block 1418 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1418 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1406 151 198 minecraft:air if block 1406 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1406 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1410 151 202 minecraft:air if block 1410 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1410 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1407 151 201 minecraft:air if block 1407 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1407 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1409 151 199 minecraft:air if block 1409 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1409 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1414 151 201 minecraft:air if block 1414 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1414 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1414 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1414 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b390 ftm_brick 2
