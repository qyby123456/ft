scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1409 91 100 minecraft:air if block 1409 92 100 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1409 91 100 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1426 91 115 minecraft:air if block 1426 92 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1426 91 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1425 91 95 minecraft:air if block 1425 92 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1425 91 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1406 91 115 minecraft:air if block 1406 92 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1406 91 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1416 91 109 minecraft:air if block 1416 92 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1416 91 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1419 91 101 minecraft:air if block 1419 92 101 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1419 91 101 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1414 91 95 minecraft:air if block 1414 92 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1414 91 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1424 91 106 minecraft:air if block 1424 92 106 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1424 91 106 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1410 91 100 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1410 91 100 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b218 ftm_brick 2
