scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1268 91 23 minecraft:air if block 1268 92 23 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1268 91 23 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1285 91 38 minecraft:air if block 1285 92 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1285 91 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1284 91 18 minecraft:air if block 1284 92 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1284 91 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1265 91 38 minecraft:air if block 1265 92 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1265 91 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1275 91 32 minecraft:air if block 1275 92 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1275 91 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1278 91 24 minecraft:air if block 1278 92 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1278 91 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1273 91 18 minecraft:air if block 1273 92 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1273 91 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1283 91 29 minecraft:air if block 1283 92 29 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1283 91 29 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1269 91 23 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1269 91 23 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b216 ftm_brick 2
