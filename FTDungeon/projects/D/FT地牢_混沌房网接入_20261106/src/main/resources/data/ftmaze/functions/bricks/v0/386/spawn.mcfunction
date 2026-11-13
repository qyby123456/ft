scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1287 211 198 minecraft:air if block 1287 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1287 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1277 211 202 minecraft:air if block 1277 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1277 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1292 211 202 minecraft:air if block 1292 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1292 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1280 211 198 minecraft:air if block 1280 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1280 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1284 211 202 minecraft:air if block 1284 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1284 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1281 211 201 minecraft:air if block 1281 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1281 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1283 211 199 minecraft:air if block 1283 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1283 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1288 211 201 minecraft:air if block 1288 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1288 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1288 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1288 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b386 ftm_brick 2
