scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 243 199 212 minecraft:air if block 243 200 212 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 243 199 212 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 236 199 225 minecraft:air if block 236 200 225 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 236 199 225 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 247 199 222 minecraft:air if block 247 200 222 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 247 199 222 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 235 199 215 minecraft:air if block 235 200 215 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 235 199 215 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 244 199 212 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 244 199 212 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b264 ftm_brick 2
