scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1269 211 198 minecraft:air if block 1269 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1269 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1259 211 202 minecraft:air if block 1259 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1259 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1274 211 202 minecraft:air if block 1274 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1274 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1262 211 198 minecraft:air if block 1262 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1262 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1266 211 202 minecraft:air if block 1266 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1266 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1263 211 201 minecraft:air if block 1263 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1263 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1265 211 199 minecraft:air if block 1265 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1265 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1270 211 201 minecraft:air if block 1270 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1270 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1270 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1270 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b403 ftm_brick 2
