scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1409 97 58 minecraft:air if block 1409 98 58 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1409 97 58 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1426 97 72 minecraft:air if block 1426 98 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1426 97 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1424 97 52 minecraft:air if block 1424 98 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1424 97 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1406 97 72 minecraft:air if block 1406 98 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1406 97 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1416 97 66 minecraft:air if block 1416 98 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1416 97 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1420 97 59 minecraft:air if block 1420 98 59 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1420 97 59 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1413 97 52 minecraft:air if block 1413 98 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1413 97 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1424 97 64 minecraft:air if block 1424 98 64 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1424 97 64 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1410 97 58 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1410 97 58 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b222 ftm_brick 2
