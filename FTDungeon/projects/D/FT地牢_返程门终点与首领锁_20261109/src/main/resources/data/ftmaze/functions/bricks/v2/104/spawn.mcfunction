scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 227 211 37 minecraft:air if block 227 212 37 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 227 211 37 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 216 211 30 minecraft:air if block 216 212 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 216 211 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 218 211 44 minecraft:air if block 218 212 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 218 211 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 219 211 36 minecraft:air if block 219 212 36 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 219 211 36 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 228 211 37 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 228 211 37 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b104 ftm_brick 2
