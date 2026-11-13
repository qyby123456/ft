scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1437 151 198 minecraft:air if block 1437 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1437 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1427 151 202 minecraft:air if block 1427 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1427 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1442 151 202 minecraft:air if block 1442 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1442 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1430 151 198 minecraft:air if block 1430 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1430 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1434 151 202 minecraft:air if block 1434 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1434 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1431 151 201 minecraft:air if block 1431 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1431 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1433 151 199 minecraft:air if block 1433 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1433 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1438 151 201 minecraft:air if block 1438 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1438 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1438 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1438 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b403 ftm_brick 2
