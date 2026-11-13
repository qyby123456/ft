scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1268 91 62 minecraft:air if block 1268 92 62 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1268 91 62 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1285 91 54 minecraft:air if block 1285 92 54 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1285 91 54 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1280 91 72 minecraft:air if block 1280 92 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1280 91 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1272 91 52 minecraft:air if block 1272 92 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1272 91 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1277 91 59 minecraft:air if block 1277 92 59 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1277 91 59 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1265 91 70 minecraft:air if block 1265 92 70 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1265 91 70 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1274 91 67 minecraft:air if block 1274 92 67 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1274 91 67 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1283 91 64 minecraft:air if block 1283 92 64 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1283 91 64 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1269 91 62 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1269 91 62 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b213 ftm_brick 2
