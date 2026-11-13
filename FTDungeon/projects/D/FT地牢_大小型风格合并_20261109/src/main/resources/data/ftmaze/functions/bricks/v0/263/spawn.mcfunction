scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 238 199 223 minecraft:air if block 238 200 223 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 238 199 223 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 247 199 215 minecraft:air if block 247 200 215 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 247 199 215 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 244 199 226 minecraft:air if block 244 200 226 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 244 199 226 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 240 199 217 minecraft:air if block 240 200 217 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 240 199 217 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 238 199 224 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 238 199 224 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b263 ftm_brick 2
