scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1358 31 71 minecraft:air if block 1358 32 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1358 31 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1375 31 86 minecraft:air if block 1375 32 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1375 31 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1374 31 66 minecraft:air if block 1374 32 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1374 31 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1355 31 86 minecraft:air if block 1355 32 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1355 31 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1365 31 80 minecraft:air if block 1365 32 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1365 31 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1368 31 72 minecraft:air if block 1368 32 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1368 31 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1363 31 66 minecraft:air if block 1363 32 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1363 31 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1373 31 77 minecraft:air if block 1373 32 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1373 31 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1359 31 71 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1359 31 71 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b221 ftm_brick 2
