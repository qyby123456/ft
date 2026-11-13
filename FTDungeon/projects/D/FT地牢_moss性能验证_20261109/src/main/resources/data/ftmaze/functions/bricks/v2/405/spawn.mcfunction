scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1437 31 198 minecraft:air if block 1437 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1437 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1427 31 202 minecraft:air if block 1427 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1427 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1442 31 202 minecraft:air if block 1442 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1442 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1430 31 198 minecraft:air if block 1430 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1430 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1434 31 202 minecraft:air if block 1434 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1434 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1431 31 201 minecraft:air if block 1431 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1431 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1433 31 199 minecraft:air if block 1433 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1433 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1438 31 201 minecraft:air if block 1438 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1438 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1438 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1438 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b405 ftm_brick 2
