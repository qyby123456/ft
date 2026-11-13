scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1353 211 198 minecraft:air if block 1353 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1353 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1343 211 202 minecraft:air if block 1343 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1343 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1358 211 202 minecraft:air if block 1358 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1358 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1346 211 198 minecraft:air if block 1346 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1346 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1350 211 202 minecraft:air if block 1350 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1350 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1347 211 201 minecraft:air if block 1347 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1347 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1349 211 199 minecraft:air if block 1349 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1349 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1354 211 201 minecraft:air if block 1354 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1354 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1354 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1354 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b405 ftm_brick 2
