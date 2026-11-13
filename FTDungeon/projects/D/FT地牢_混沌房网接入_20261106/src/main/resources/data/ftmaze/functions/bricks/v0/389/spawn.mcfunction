scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1413 211 198 minecraft:air if block 1413 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1413 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1403 211 202 minecraft:air if block 1403 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1403 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1418 211 202 minecraft:air if block 1418 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1418 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1406 211 198 minecraft:air if block 1406 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1406 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1410 211 202 minecraft:air if block 1410 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1410 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1407 211 201 minecraft:air if block 1407 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1407 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1409 211 199 minecraft:air if block 1409 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1409 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1414 211 201 minecraft:air if block 1414 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1414 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1414 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1414 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b389 ftm_brick 2
