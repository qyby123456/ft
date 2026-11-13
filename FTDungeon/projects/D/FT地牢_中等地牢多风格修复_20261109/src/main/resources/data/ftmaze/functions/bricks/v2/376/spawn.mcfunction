scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1347 211 198 minecraft:air if block 1347 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1347 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1337 211 202 minecraft:air if block 1337 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1337 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1352 211 202 minecraft:air if block 1352 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1352 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1340 211 198 minecraft:air if block 1340 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1340 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1344 211 202 minecraft:air if block 1344 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1344 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1341 211 201 minecraft:air if block 1341 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1341 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1343 211 199 minecraft:air if block 1343 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1343 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1348 211 201 minecraft:air if block 1348 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1348 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1348 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1348 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b376 ftm_brick 2
