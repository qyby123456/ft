scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1297 91 198 minecraft:air if block 1297 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1297 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1287 91 202 minecraft:air if block 1287 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1287 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1302 91 202 minecraft:air if block 1302 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1302 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1290 91 198 minecraft:air if block 1290 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1290 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1294 91 202 minecraft:air if block 1294 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1294 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1291 91 201 minecraft:air if block 1291 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1291 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1293 91 199 minecraft:air if block 1293 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1293 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1298 91 201 minecraft:air if block 1298 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1298 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1298 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1298 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b372 ftm_brick 2
