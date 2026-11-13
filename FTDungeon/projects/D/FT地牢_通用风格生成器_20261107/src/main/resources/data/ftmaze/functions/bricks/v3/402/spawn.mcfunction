scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1395 151 198 minecraft:air if block 1395 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1395 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1385 151 202 minecraft:air if block 1385 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1385 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1400 151 202 minecraft:air if block 1400 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1400 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1388 151 198 minecraft:air if block 1388 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1388 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1392 151 202 minecraft:air if block 1392 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1392 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1389 151 201 minecraft:air if block 1389 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1389 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1391 151 199 minecraft:air if block 1391 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1391 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1396 151 201 minecraft:air if block 1396 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1396 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1396 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1396 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b402 ftm_brick 2
