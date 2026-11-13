scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1358 31 31 minecraft:air if block 1358 32 31 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1358 31 31 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1375 31 44 minecraft:air if block 1375 32 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1375 31 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1373 31 25 minecraft:air if block 1373 32 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1373 31 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1355 31 44 minecraft:air if block 1355 32 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1355 31 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1365 31 39 minecraft:air if block 1365 32 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1365 31 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1369 31 32 minecraft:air if block 1369 32 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1369 31 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1362 31 25 minecraft:air if block 1362 32 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1362 31 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1373 31 37 minecraft:air if block 1373 32 37 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1373 31 37 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1359 31 31 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1359 31 31 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b224 ftm_brick 2
