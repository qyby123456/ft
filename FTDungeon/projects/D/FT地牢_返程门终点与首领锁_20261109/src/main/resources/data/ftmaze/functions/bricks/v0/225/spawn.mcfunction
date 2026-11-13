scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1409 97 25 minecraft:air if block 1409 98 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1409 97 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1426 97 17 minecraft:air if block 1426 98 17 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1426 97 17 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1421 97 33 minecraft:air if block 1421 98 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1421 97 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1414 97 16 minecraft:air if block 1414 98 16 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1414 97 16 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1419 97 23 minecraft:air if block 1419 98 23 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1419 97 23 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1406 97 33 minecraft:air if block 1406 98 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1406 97 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1406 97 18 minecraft:air if block 1406 98 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1406 97 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1414 97 30 minecraft:air if block 1414 98 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1414 97 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1410 97 25 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1410 97 25 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b225 ftm_brick 2
