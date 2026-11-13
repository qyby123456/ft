scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1268 211 62 minecraft:air if block 1268 212 62 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1268 211 62 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1284 211 77 minecraft:air if block 1284 212 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1284 211 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1284 211 57 minecraft:air if block 1284 212 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1284 211 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1264 211 77 minecraft:air if block 1264 212 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1264 211 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1274 211 71 minecraft:air if block 1274 212 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1274 211 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1281 211 66 minecraft:air if block 1281 212 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1281 211 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1273 211 57 minecraft:air if block 1273 212 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1273 211 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1264 211 57 minecraft:air if block 1264 212 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1264 211 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1268 211 61 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1268 211 61 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b213 ftm_brick 2
