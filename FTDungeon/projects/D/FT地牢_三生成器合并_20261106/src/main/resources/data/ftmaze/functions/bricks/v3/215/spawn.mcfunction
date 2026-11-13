scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1217 215 29 minecraft:air if block 1217 216 29 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1217 215 29 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1234 215 19 minecraft:air if block 1234 216 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1234 215 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1230 215 39 minecraft:air if block 1230 216 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1230 215 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1214 215 19 minecraft:air if block 1214 216 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1214 215 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1214 215 39 minecraft:air if block 1214 216 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1214 215 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1224 215 22 minecraft:air if block 1224 216 22 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1224 215 22 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1231 215 28 minecraft:air if block 1231 216 28 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1231 215 28 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1222 215 35 minecraft:air if block 1222 216 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1222 215 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1218 215 29 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1218 215 29 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b215 ftm_brick 2
