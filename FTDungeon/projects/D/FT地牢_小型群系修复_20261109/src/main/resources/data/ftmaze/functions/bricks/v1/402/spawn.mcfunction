scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1227 211 198 minecraft:air if block 1227 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1227 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1217 211 202 minecraft:air if block 1217 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1217 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1232 211 202 minecraft:air if block 1232 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1232 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1220 211 198 minecraft:air if block 1220 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1220 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1224 211 202 minecraft:air if block 1224 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1224 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1221 211 201 minecraft:air if block 1221 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1221 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1223 211 199 minecraft:air if block 1223 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1223 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1228 211 201 minecraft:air if block 1228 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1228 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1228 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1228 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b402 ftm_brick 2
