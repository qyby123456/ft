scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1358 91 71 minecraft:air if block 1358 92 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1358 91 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1375 91 86 minecraft:air if block 1375 92 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1375 91 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1374 91 66 minecraft:air if block 1374 92 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1374 91 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1355 91 86 minecraft:air if block 1355 92 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1355 91 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1365 91 80 minecraft:air if block 1365 92 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1365 91 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1368 91 72 minecraft:air if block 1368 92 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1368 91 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1363 91 66 minecraft:air if block 1363 92 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1363 91 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1373 91 77 minecraft:air if block 1373 92 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1373 91 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1359 91 71 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1359 91 71 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b221 ftm_brick 2
