scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1287 91 198 minecraft:air if block 1287 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1287 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1277 91 202 minecraft:air if block 1277 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1277 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1292 91 202 minecraft:air if block 1292 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1292 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1280 91 198 minecraft:air if block 1280 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1280 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1284 91 202 minecraft:air if block 1284 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1284 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1281 91 201 minecraft:air if block 1281 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1281 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1283 91 199 minecraft:air if block 1283 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1283 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1288 91 201 minecraft:air if block 1288 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1288 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1288 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1288 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b386 ftm_brick 2
