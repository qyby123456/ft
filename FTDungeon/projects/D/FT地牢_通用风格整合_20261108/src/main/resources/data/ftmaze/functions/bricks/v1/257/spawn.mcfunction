scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 238 67 243 minecraft:air if block 238 68 243 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 238 67 243 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 247 67 253 minecraft:air if block 247 68 253 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 247 67 253 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 239 67 251 minecraft:air if block 239 68 251 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 239 67 251 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 247 67 245 minecraft:air if block 247 68 245 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 247 67 245 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 238 67 244 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 238 67 244 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b257 ftm_brick 2
