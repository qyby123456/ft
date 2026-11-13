scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 227 151 66 minecraft:air if block 227 152 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 227 151 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 216 151 59 minecraft:air if block 216 152 59 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 216 151 59 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 218 151 73 minecraft:air if block 218 152 73 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 218 151 73 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 219 151 65 minecraft:air if block 219 152 65 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 219 151 65 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 228 151 66 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 228 151 66 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b124 ftm_brick 2
