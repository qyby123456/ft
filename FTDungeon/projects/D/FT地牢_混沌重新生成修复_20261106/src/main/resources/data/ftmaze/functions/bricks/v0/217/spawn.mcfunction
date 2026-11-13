scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1409 91 135 minecraft:air if block 1409 92 135 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1409 91 135 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1426 91 148 minecraft:air if block 1426 92 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1426 91 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1424 91 130 minecraft:air if block 1424 92 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1424 91 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1406 91 148 minecraft:air if block 1406 92 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1406 91 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1416 91 143 minecraft:air if block 1416 92 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1416 91 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1414 91 129 minecraft:air if block 1414 92 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1414 91 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1419 91 135 minecraft:air if block 1419 92 135 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1419 91 135 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1424 91 140 minecraft:air if block 1424 92 140 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1424 91 140 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1410 91 135 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1410 91 135 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b217 ftm_brick 2
