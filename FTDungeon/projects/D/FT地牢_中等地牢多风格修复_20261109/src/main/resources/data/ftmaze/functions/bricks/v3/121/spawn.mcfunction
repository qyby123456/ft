scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 227 151 42 minecraft:air if block 227 152 42 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 227 151 42 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 216 151 52 minecraft:air if block 216 152 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 216 151 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 216 151 34 minecraft:air if block 216 152 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 216 151 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 228 151 52 minecraft:air if block 228 152 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 228 151 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 228 151 42 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 228 151 42 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b121 ftm_brick 2
