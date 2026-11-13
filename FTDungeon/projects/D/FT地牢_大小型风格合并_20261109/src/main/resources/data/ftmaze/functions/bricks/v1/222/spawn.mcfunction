scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1401 91 71 minecraft:air if block 1401 92 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1401 91 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1418 91 86 minecraft:air if block 1418 92 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1418 91 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1417 91 66 minecraft:air if block 1417 92 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1417 91 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1398 91 86 minecraft:air if block 1398 92 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1398 91 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1408 91 80 minecraft:air if block 1408 92 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1408 91 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1411 91 72 minecraft:air if block 1411 92 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1411 91 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1406 91 66 minecraft:air if block 1406 92 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1406 91 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1416 91 77 minecraft:air if block 1416 92 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1416 91 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1402 91 71 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1402 91 71 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b222 ftm_brick 2
