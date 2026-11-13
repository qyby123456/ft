scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 227 211 49 minecraft:air if block 227 212 49 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 227 211 49 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 216 211 41 minecraft:air if block 216 212 41 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 216 211 41 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 217 211 57 minecraft:air if block 217 212 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 217 211 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 218 211 48 minecraft:air if block 218 212 48 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 218 211 48 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 228 211 49 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 228 211 49 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b107 ftm_brick 2
