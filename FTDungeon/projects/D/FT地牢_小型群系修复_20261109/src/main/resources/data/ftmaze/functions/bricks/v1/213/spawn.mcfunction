scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1268 31 62 minecraft:air if block 1268 32 62 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1268 31 62 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1285 31 54 minecraft:air if block 1285 32 54 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1285 31 54 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1280 31 72 minecraft:air if block 1280 32 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1280 31 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1272 31 52 minecraft:air if block 1272 32 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1272 31 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1277 31 59 minecraft:air if block 1277 32 59 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1277 31 59 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1265 31 70 minecraft:air if block 1265 32 70 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1265 31 70 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1274 31 67 minecraft:air if block 1274 32 67 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1274 31 67 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1283 31 64 minecraft:air if block 1283 32 64 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1283 31 64 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1269 31 62 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1269 31 62 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b213 ftm_brick 2
