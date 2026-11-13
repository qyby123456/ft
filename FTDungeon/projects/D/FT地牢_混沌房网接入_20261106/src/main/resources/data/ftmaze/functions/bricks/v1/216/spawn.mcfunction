scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1268 31 23 minecraft:air if block 1268 32 23 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1268 31 23 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1285 31 38 minecraft:air if block 1285 32 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1285 31 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1284 31 18 minecraft:air if block 1284 32 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1284 31 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1265 31 38 minecraft:air if block 1265 32 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1265 31 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1275 31 32 minecraft:air if block 1275 32 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1275 31 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1278 31 24 minecraft:air if block 1278 32 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1278 31 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1273 31 18 minecraft:air if block 1273 32 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1273 31 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1283 31 29 minecraft:air if block 1283 32 29 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1283 31 29 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1269 31 23 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1269 31 23 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b216 ftm_brick 2
