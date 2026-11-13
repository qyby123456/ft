scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1358 211 112 minecraft:air if block 1358 212 112 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1358 211 112 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1375 211 126 minecraft:air if block 1375 212 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1375 211 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1373 211 107 minecraft:air if block 1373 212 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1373 211 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1355 211 126 minecraft:air if block 1355 212 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1355 211 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1365 211 120 minecraft:air if block 1365 212 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1365 211 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1363 211 107 minecraft:air if block 1363 212 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1363 211 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1368 211 112 minecraft:air if block 1368 212 112 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1368 211 112 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1373 211 117 minecraft:air if block 1373 212 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1373 211 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1359 211 112 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1359 211 112 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b219 ftm_brick 2
