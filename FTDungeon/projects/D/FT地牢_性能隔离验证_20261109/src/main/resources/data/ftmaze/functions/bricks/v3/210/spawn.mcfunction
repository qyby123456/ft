scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1267 211 115 minecraft:air if block 1267 212 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1267 211 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1282 211 129 minecraft:air if block 1282 212 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1282 211 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1281 211 109 minecraft:air if block 1281 212 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1281 211 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1264 211 129 minecraft:air if block 1264 212 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1264 211 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1273 211 123 minecraft:air if block 1273 212 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1273 211 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1278 211 117 minecraft:air if block 1278 212 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1278 211 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1271 211 109 minecraft:air if block 1271 212 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1271 211 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1263 211 120 minecraft:air if block 1263 212 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1263 211 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1267 211 114 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1267 211 114 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b210 ftm_brick 2
