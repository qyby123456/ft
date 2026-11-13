scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 232 187 243 minecraft:air if block 232 188 243 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 232 187 243 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 246 187 253 minecraft:air if block 246 188 253 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 246 187 253 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 234 187 254 minecraft:air if block 234 188 254 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 234 187 254 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 244 187 242 minecraft:air if block 244 188 242 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 244 187 242 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 233 187 243 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 233 187 243 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b263 ftm_brick 2
