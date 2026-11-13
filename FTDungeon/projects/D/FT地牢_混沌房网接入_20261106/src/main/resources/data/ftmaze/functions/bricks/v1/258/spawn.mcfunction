scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 245 79 211 minecraft:air if block 245 80 211 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 245 79 211 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 235 79 225 minecraft:air if block 235 80 225 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 235 79 225 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 246 79 223 minecraft:air if block 246 80 223 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 246 79 223 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 237 79 215 minecraft:air if block 237 80 215 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 237 79 215 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 245 79 212 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 245 79 212 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b258 ftm_brick 2
