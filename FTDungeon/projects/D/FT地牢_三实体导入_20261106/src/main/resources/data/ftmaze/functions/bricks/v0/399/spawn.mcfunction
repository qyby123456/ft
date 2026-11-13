scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1269 31 198 minecraft:air if block 1269 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1269 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1259 31 202 minecraft:air if block 1259 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1259 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1274 31 202 minecraft:air if block 1274 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1274 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1262 31 198 minecraft:air if block 1262 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1262 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1266 31 202 minecraft:air if block 1266 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1266 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1263 31 201 minecraft:air if block 1263 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1263 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1265 31 199 minecraft:air if block 1265 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1265 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1270 31 201 minecraft:air if block 1270 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1270 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1270 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1270 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b399 ftm_brick 2
