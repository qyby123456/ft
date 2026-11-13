scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1358 211 71 minecraft:air if block 1358 212 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1358 211 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1375 211 86 minecraft:air if block 1375 212 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1375 211 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1374 211 66 minecraft:air if block 1374 212 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1374 211 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1355 211 86 minecraft:air if block 1355 212 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1355 211 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1365 211 80 minecraft:air if block 1365 212 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1365 211 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1368 211 72 minecraft:air if block 1368 212 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1368 211 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1363 211 66 minecraft:air if block 1363 212 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1363 211 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1373 211 77 minecraft:air if block 1373 212 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1373 211 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1359 211 71 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1359 211 71 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b221 ftm_brick 2
