scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1401 211 71 minecraft:air if block 1401 212 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1401 211 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1418 211 86 minecraft:air if block 1418 212 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1418 211 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1417 211 66 minecraft:air if block 1417 212 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1417 211 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1398 211 86 minecraft:air if block 1398 212 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1398 211 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1408 211 80 minecraft:air if block 1408 212 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1408 211 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1411 211 72 minecraft:air if block 1411 212 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1411 211 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1406 211 66 minecraft:air if block 1406 212 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1406 211 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1416 211 77 minecraft:air if block 1416 212 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1416 211 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1402 211 71 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1402 211 71 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b222 ftm_brick 2
