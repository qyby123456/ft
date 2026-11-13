scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1347 31 198 minecraft:air if block 1347 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1347 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1337 31 202 minecraft:air if block 1337 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1337 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1352 31 202 minecraft:air if block 1352 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1352 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1340 31 198 minecraft:air if block 1340 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1340 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1344 31 202 minecraft:air if block 1344 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1344 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1341 31 201 minecraft:air if block 1341 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1341 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1343 31 199 minecraft:air if block 1343 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1343 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1348 31 201 minecraft:air if block 1348 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1348 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1348 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1348 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b375 ftm_brick 2
