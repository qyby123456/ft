scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 227 151 49 minecraft:air if block 227 152 49 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 227 151 49 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 216 151 41 minecraft:air if block 216 152 41 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 216 151 41 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 217 151 57 minecraft:air if block 217 152 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 217 151 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 218 151 48 minecraft:air if block 218 152 48 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 218 151 48 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 228 151 49 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 228 151 49 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b121 ftm_brick 2
