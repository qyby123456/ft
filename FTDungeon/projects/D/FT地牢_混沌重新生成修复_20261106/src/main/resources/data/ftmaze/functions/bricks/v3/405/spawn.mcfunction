scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1521 151 198 minecraft:air if block 1521 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1521 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1511 151 202 minecraft:air if block 1511 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1511 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1526 151 202 minecraft:air if block 1526 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1526 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1514 151 198 minecraft:air if block 1514 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1514 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1518 151 202 minecraft:air if block 1518 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1518 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1515 151 201 minecraft:air if block 1515 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1515 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1517 151 199 minecraft:air if block 1517 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1517 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1522 151 201 minecraft:air if block 1522 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1522 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1522 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1522 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b405 ftm_brick 2
