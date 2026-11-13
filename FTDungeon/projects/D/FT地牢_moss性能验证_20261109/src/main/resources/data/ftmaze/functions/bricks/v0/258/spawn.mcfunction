scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 238 151 223 minecraft:air if block 238 152 223 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 238 151 223 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 247 151 215 minecraft:air if block 247 152 215 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 247 151 215 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 244 151 226 minecraft:air if block 244 152 226 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 244 151 226 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 240 151 217 minecraft:air if block 240 152 217 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 240 151 217 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 238 151 224 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 238 151 224 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b258 ftm_brick 2
