scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1413 91 198 minecraft:air if block 1413 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1413 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1403 91 202 minecraft:air if block 1403 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1403 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1418 91 202 minecraft:air if block 1418 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1418 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1406 91 198 minecraft:air if block 1406 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1406 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1410 91 202 minecraft:air if block 1410 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1410 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1407 91 201 minecraft:air if block 1407 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1407 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1409 91 199 minecraft:air if block 1409 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1409 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1414 91 201 minecraft:air if block 1414 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1414 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1414 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1414 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b389 ftm_brick 2
