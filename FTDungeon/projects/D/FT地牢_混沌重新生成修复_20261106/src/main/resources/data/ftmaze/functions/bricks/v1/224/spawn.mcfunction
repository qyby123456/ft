scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1358 91 31 minecraft:air if block 1358 92 31 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1358 91 31 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1375 91 44 minecraft:air if block 1375 92 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1375 91 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1373 91 25 minecraft:air if block 1373 92 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1373 91 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1355 91 44 minecraft:air if block 1355 92 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1355 91 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1365 91 39 minecraft:air if block 1365 92 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1365 91 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1369 91 32 minecraft:air if block 1369 92 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1369 91 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1362 91 25 minecraft:air if block 1362 92 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1362 91 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1373 91 37 minecraft:air if block 1373 92 37 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1373 91 37 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1359 91 31 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1359 91 31 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b224 ftm_brick 2
