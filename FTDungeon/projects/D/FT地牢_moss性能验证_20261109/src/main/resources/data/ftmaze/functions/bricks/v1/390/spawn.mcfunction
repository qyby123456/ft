scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1287 151 198 minecraft:air if block 1287 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1287 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1277 151 202 minecraft:air if block 1277 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1277 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1292 151 202 minecraft:air if block 1292 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1292 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1280 151 198 minecraft:air if block 1280 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1280 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1284 151 202 minecraft:air if block 1284 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1284 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1281 151 201 minecraft:air if block 1281 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1281 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1283 151 199 minecraft:air if block 1283 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1283 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1288 151 201 minecraft:air if block 1288 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1288 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1288 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1288 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b390 ftm_brick 2
