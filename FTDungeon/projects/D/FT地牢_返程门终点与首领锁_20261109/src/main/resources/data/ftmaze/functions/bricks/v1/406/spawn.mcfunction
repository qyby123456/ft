scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1395 211 198 minecraft:air if block 1395 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1395 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1385 211 202 minecraft:air if block 1385 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1385 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1400 211 202 minecraft:air if block 1400 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1400 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1388 211 198 minecraft:air if block 1388 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1388 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1392 211 202 minecraft:air if block 1392 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1392 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1389 211 201 minecraft:air if block 1389 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1389 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1391 211 199 minecraft:air if block 1391 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1391 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1396 211 201 minecraft:air if block 1396 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1396 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1396 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1396 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b406 ftm_brick 2
