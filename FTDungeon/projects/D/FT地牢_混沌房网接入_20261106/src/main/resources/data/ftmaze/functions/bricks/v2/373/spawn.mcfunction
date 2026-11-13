scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1221 211 198 minecraft:air if block 1221 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1221 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1211 211 202 minecraft:air if block 1211 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1211 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1226 211 202 minecraft:air if block 1226 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1226 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1214 211 198 minecraft:air if block 1214 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1214 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1218 211 202 minecraft:air if block 1218 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1218 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1215 211 201 minecraft:air if block 1215 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1215 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1217 211 199 minecraft:air if block 1217 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1217 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1222 211 201 minecraft:air if block 1222 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1222 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1222 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1222 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b373 ftm_brick 2
