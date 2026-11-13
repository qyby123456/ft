scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1414 91 167 minecraft:air if block 1414 92 167 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1414 91 167 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1417 91 159 minecraft:air if block 1417 92 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1417 91 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1419 91 171 minecraft:air if block 1419 92 171 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1419 91 171 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1413 91 161 minecraft:air if block 1413 92 161 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1413 91 161 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1413 91 167 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1413 91 167 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b375 ftm_brick 2
