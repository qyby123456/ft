scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 232 187 244 minecraft:air if block 232 188 244 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 232 187 244 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 246 187 254 minecraft:air if block 246 188 254 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 246 187 254 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 244 187 243 minecraft:air if block 244 188 243 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 244 187 243 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 236 187 252 minecraft:air if block 236 188 252 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 236 187 252 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 233 187 244 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 233 187 244 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b263 ftm_brick 2
