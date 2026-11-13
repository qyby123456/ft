scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 243 79 212 minecraft:air if block 243 80 212 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 243 79 212 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 236 79 225 minecraft:air if block 236 80 225 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 236 79 225 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 247 79 222 minecraft:air if block 247 80 222 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 247 79 222 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 235 79 215 minecraft:air if block 235 80 215 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 235 79 215 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 244 79 212 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 244 79 212 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b257 ftm_brick 2
