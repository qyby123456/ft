scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 227 211 19 minecraft:air if block 227 212 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 227 211 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 216 211 15 minecraft:air if block 216 212 15 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 216 211 15 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 228 211 19 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 228 211 19 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b108 ftm_brick 2
