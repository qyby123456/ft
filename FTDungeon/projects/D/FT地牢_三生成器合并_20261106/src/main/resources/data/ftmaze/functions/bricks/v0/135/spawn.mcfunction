scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 227 91 49 minecraft:air if block 227 92 49 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 227 91 49 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 216 91 41 minecraft:air if block 216 92 41 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 216 91 41 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 217 91 57 minecraft:air if block 217 92 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 217 91 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 218 91 48 minecraft:air if block 218 92 48 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 218 91 48 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 228 91 49 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 228 91 49 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b135 ftm_brick 2
