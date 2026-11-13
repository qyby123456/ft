scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1358 211 31 minecraft:air if block 1358 212 31 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1358 211 31 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1375 211 44 minecraft:air if block 1375 212 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1375 211 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1373 211 25 minecraft:air if block 1373 212 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1373 211 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1355 211 44 minecraft:air if block 1355 212 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1355 211 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1365 211 39 minecraft:air if block 1365 212 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1365 211 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1369 211 32 minecraft:air if block 1369 212 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1369 211 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1362 211 25 minecraft:air if block 1362 212 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1362 211 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1373 211 37 minecraft:air if block 1373 212 37 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1373 211 37 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1359 211 31 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1359 211 31 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b224 ftm_brick 2
