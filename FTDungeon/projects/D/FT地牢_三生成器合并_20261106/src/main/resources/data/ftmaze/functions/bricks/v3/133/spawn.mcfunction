scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 227 91 37 minecraft:air if block 227 92 37 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 227 91 37 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 216 91 30 minecraft:air if block 216 92 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 216 91 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 218 91 44 minecraft:air if block 218 92 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 218 91 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 219 91 36 minecraft:air if block 219 92 36 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 219 91 36 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 228 91 37 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 228 91 37 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b133 ftm_brick 2
