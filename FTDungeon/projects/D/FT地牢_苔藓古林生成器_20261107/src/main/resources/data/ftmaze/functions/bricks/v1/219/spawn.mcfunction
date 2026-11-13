scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1358 91 112 minecraft:air if block 1358 92 112 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1358 91 112 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1375 91 126 minecraft:air if block 1375 92 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1375 91 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1373 91 107 minecraft:air if block 1373 92 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1373 91 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1355 91 126 minecraft:air if block 1355 92 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1355 91 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1365 91 120 minecraft:air if block 1365 92 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1365 91 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1363 91 107 minecraft:air if block 1363 92 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1363 91 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1368 91 112 minecraft:air if block 1368 92 112 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1368 91 112 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1373 91 117 minecraft:air if block 1373 92 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1373 91 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1359 91 112 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1359 91 112 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b219 ftm_brick 2
