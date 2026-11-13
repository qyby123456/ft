scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1401 31 71 minecraft:air if block 1401 32 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1401 31 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1418 31 86 minecraft:air if block 1418 32 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1418 31 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1417 31 66 minecraft:air if block 1417 32 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1417 31 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1398 31 86 minecraft:air if block 1398 32 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1398 31 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1408 31 80 minecraft:air if block 1408 32 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1408 31 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1411 31 72 minecraft:air if block 1411 32 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1411 31 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1406 31 66 minecraft:air if block 1406 32 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1406 31 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1416 31 77 minecraft:air if block 1416 32 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1416 31 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1402 31 71 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1402 31 71 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b222 ftm_brick 2
