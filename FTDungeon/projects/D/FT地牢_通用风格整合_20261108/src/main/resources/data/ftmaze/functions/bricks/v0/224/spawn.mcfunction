scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1358 91 24 minecraft:air if block 1358 92 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1358 91 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1375 91 38 minecraft:air if block 1375 92 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1375 91 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1373 91 18 minecraft:air if block 1373 92 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1373 91 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1355 91 38 minecraft:air if block 1355 92 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1355 91 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1365 91 32 minecraft:air if block 1365 92 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1365 91 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1369 91 25 minecraft:air if block 1369 92 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1369 91 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1362 91 18 minecraft:air if block 1362 92 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1362 91 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1373 91 30 minecraft:air if block 1373 92 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1373 91 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1359 91 24 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1359 91 24 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b224 ftm_brick 2
