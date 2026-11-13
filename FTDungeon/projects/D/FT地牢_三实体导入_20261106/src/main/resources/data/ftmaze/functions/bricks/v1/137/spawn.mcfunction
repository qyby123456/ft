scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 227 91 66 minecraft:air if block 227 92 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 227 91 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 216 91 59 minecraft:air if block 216 92 59 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 216 91 59 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 218 91 73 minecraft:air if block 218 92 73 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 218 91 73 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 219 91 65 minecraft:air if block 219 92 65 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 219 91 65 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 228 91 66 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 228 91 66 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b137 ftm_brick 2
