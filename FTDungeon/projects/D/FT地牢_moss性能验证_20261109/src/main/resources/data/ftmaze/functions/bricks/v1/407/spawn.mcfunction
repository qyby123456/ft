scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1437 211 198 minecraft:air if block 1437 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1437 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1427 211 202 minecraft:air if block 1427 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1427 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1442 211 202 minecraft:air if block 1442 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1442 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1430 211 198 minecraft:air if block 1430 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1430 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1434 211 202 minecraft:air if block 1434 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1434 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1431 211 201 minecraft:air if block 1431 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1431 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1433 211 199 minecraft:air if block 1433 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1433 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1438 211 201 minecraft:air if block 1438 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1438 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1438 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1438 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b407 ftm_brick 2
