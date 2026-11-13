scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1455 211 198 minecraft:air if block 1455 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1455 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1445 211 202 minecraft:air if block 1445 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1445 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1460 211 202 minecraft:air if block 1460 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1460 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1448 211 198 minecraft:air if block 1448 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1448 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1452 211 202 minecraft:air if block 1452 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1452 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1449 211 201 minecraft:air if block 1449 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1449 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1451 211 199 minecraft:air if block 1451 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1451 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1456 211 201 minecraft:air if block 1456 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1456 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1456 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1456 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b390 ftm_brick 2
