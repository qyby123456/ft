scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1401 33 34 minecraft:air if block 1401 34 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1401 33 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1418 33 24 minecraft:air if block 1418 34 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1418 33 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1414 33 44 minecraft:air if block 1414 34 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1414 33 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1398 33 24 minecraft:air if block 1398 34 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1398 33 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1398 33 44 minecraft:air if block 1398 34 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1398 33 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1408 33 27 minecraft:air if block 1408 34 27 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1408 33 27 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1415 33 33 minecraft:air if block 1415 34 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1415 33 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1406 33 40 minecraft:air if block 1406 34 40 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1406 33 40 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1402 33 34 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1402 33 34 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b225 ftm_brick 2
