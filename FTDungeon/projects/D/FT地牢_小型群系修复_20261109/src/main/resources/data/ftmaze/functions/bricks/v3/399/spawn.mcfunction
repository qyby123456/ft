scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1269 151 198 minecraft:air if block 1269 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1269 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1259 151 202 minecraft:air if block 1259 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1259 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1274 151 202 minecraft:air if block 1274 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1274 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1262 151 198 minecraft:air if block 1262 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1262 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1266 151 202 minecraft:air if block 1266 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1266 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1263 151 201 minecraft:air if block 1263 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1263 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1265 151 199 minecraft:air if block 1265 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1265 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1270 151 201 minecraft:air if block 1270 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1270 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1270 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1270 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b399 ftm_brick 2
