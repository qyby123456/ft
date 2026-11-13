scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1353 31 198 minecraft:air if block 1353 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1353 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1343 31 202 minecraft:air if block 1343 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1343 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1358 31 202 minecraft:air if block 1358 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1358 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1346 31 198 minecraft:air if block 1346 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1346 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1350 31 202 minecraft:air if block 1350 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1350 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1347 31 201 minecraft:air if block 1347 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1347 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1349 31 199 minecraft:air if block 1349 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1349 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1354 31 201 minecraft:air if block 1354 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1354 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1354 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1354 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b401 ftm_brick 2
