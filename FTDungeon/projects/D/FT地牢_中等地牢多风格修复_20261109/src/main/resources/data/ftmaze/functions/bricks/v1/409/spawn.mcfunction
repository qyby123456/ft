scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1521 211 198 minecraft:air if block 1521 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1521 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1511 211 202 minecraft:air if block 1511 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1511 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1526 211 202 minecraft:air if block 1526 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1526 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1514 211 198 minecraft:air if block 1514 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1514 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1518 211 202 minecraft:air if block 1518 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1518 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1515 211 201 minecraft:air if block 1515 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1515 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1517 211 199 minecraft:air if block 1517 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1517 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1522 211 201 minecraft:air if block 1522 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1522 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1522 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1522 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b409 ftm_brick 2
