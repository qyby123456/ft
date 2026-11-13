scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1353 151 198 minecraft:air if block 1353 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1353 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1343 151 202 minecraft:air if block 1343 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1343 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1358 151 202 minecraft:air if block 1358 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1358 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1346 151 198 minecraft:air if block 1346 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1346 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1350 151 202 minecraft:air if block 1350 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1350 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1347 151 201 minecraft:air if block 1347 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1347 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1349 151 199 minecraft:air if block 1349 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1349 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1354 151 201 minecraft:air if block 1354 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1354 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1354 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1354 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b401 ftm_brick 2
