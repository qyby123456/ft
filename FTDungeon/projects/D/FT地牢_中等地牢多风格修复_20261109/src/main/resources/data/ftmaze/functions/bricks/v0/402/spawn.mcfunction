scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1395 31 198 minecraft:air if block 1395 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1395 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1385 31 202 minecraft:air if block 1385 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1385 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1400 31 202 minecraft:air if block 1400 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1400 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1388 31 198 minecraft:air if block 1388 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1388 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1392 31 202 minecraft:air if block 1392 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1392 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1389 31 201 minecraft:air if block 1389 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1389 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1391 31 199 minecraft:air if block 1391 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1391 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1396 31 201 minecraft:air if block 1396 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1396 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1396 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1396 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b402 ftm_brick 2
