scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 238 175 223 minecraft:air if block 238 176 223 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 238 175 223 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 247 175 215 minecraft:air if block 247 176 215 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 247 175 215 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 244 175 226 minecraft:air if block 244 176 226 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 244 175 226 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 240 175 217 minecraft:air if block 240 176 217 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 240 175 217 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 238 175 224 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 238 175 224 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b262 ftm_brick 2
