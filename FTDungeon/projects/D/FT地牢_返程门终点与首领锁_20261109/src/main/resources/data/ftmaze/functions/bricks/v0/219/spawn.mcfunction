scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1361 91 98 minecraft:air if block 1361 92 98 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1361 91 98 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1376 91 115 minecraft:air if block 1376 92 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1376 91 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1358 91 114 minecraft:air if block 1358 92 114 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1358 91 114 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1376 91 95 minecraft:air if block 1376 92 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1376 91 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1368 91 107 minecraft:air if block 1368 92 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1368 91 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1377 91 105 minecraft:air if block 1377 92 105 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1377 91 105 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1357 91 104 minecraft:air if block 1357 92 104 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1357 91 104 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1370 91 99 minecraft:air if block 1370 92 99 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1370 91 99 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1361 91 99 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1361 91 99 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b219 ftm_brick 2
