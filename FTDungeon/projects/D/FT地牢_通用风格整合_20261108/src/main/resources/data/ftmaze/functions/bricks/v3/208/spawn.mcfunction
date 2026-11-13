scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1218 211 115 minecraft:air if block 1218 212 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1218 211 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1234 211 129 minecraft:air if block 1234 212 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1234 211 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1233 211 109 minecraft:air if block 1233 212 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1233 211 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1214 211 129 minecraft:air if block 1214 212 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1214 211 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1224 211 124 minecraft:air if block 1224 212 124 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1224 211 124 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1230 211 118 minecraft:air if block 1230 212 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1230 211 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1214 211 109 minecraft:air if block 1214 212 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1214 211 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1223 211 110 minecraft:air if block 1223 212 110 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1223 211 110 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1218 211 114 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1218 211 114 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b208 ftm_brick 2
