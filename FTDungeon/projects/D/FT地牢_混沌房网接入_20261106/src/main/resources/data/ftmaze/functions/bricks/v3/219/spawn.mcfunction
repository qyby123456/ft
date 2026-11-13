scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1358 31 112 minecraft:air if block 1358 32 112 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1358 31 112 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1375 31 126 minecraft:air if block 1375 32 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1375 31 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1373 31 107 minecraft:air if block 1373 32 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1373 31 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1355 31 126 minecraft:air if block 1355 32 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1355 31 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1365 31 120 minecraft:air if block 1365 32 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1365 31 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1363 31 107 minecraft:air if block 1363 32 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1363 31 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1368 31 112 minecraft:air if block 1368 32 112 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1368 31 112 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1373 31 117 minecraft:air if block 1373 32 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1373 31 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1359 31 112 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1359 31 112 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b219 ftm_brick 2
