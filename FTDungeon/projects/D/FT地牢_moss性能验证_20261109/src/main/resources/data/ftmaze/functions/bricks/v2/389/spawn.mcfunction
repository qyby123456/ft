scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1371 151 198 minecraft:air if block 1371 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1371 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1361 151 202 minecraft:air if block 1361 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1361 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1376 151 202 minecraft:air if block 1376 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1376 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1364 151 198 minecraft:air if block 1364 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1364 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1368 151 202 minecraft:air if block 1368 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1368 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1365 151 201 minecraft:air if block 1365 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1365 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1367 151 199 minecraft:air if block 1367 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1367 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1372 151 201 minecraft:air if block 1372 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1372 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1372 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1372 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b389 ftm_brick 2
