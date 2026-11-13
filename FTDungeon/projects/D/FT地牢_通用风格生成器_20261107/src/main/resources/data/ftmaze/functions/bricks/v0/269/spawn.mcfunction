scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 277 211 214 minecraft:air if block 277 212 214 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 277 211 214 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 261 211 206 minecraft:air if block 261 212 206 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 261 211 206 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 265 211 215 minecraft:air if block 265 212 215 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 265 211 215 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 271 211 203 minecraft:air if block 271 212 203 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 271 211 203 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 276 211 214 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 276 211 214 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b269 ftm_brick 2
