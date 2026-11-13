scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1219 211 76 minecraft:air if block 1219 212 76 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1219 211 76 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1234 211 88 minecraft:air if block 1234 212 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1234 211 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1233 211 68 minecraft:air if block 1233 212 68 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1233 211 68 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1214 211 88 minecraft:air if block 1214 212 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1214 211 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1214 211 68 minecraft:air if block 1214 212 68 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1214 211 68 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1227 211 81 minecraft:air if block 1227 212 81 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1227 211 81 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1221 211 85 minecraft:air if block 1221 212 85 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1221 211 85 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1222 211 69 minecraft:air if block 1222 212 69 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1222 211 69 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1218 211 76 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1218 211 76 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b212 ftm_brick 2
