scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1444 211 30 minecraft:air if block 1444 212 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1444 211 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1461 211 44 minecraft:air if block 1461 212 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1461 211 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1459 211 25 minecraft:air if block 1459 212 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1459 211 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1441 211 44 minecraft:air if block 1441 212 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1441 211 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1451 211 38 minecraft:air if block 1451 212 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1451 211 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1449 211 25 minecraft:air if block 1449 212 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1449 211 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1454 211 30 minecraft:air if block 1454 212 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1454 211 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1459 211 35 minecraft:air if block 1459 212 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1459 211 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1445 211 30 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1445 211 30 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b226 ftm_brick 2
