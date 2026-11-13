scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 243 31 212 minecraft:air if block 243 32 212 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 243 31 212 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 236 31 225 minecraft:air if block 236 32 225 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 236 31 225 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 247 31 222 minecraft:air if block 247 32 222 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 247 31 222 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 235 31 215 minecraft:air if block 235 32 215 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 235 31 215 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 244 31 212 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 244 31 212 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b253 ftm_brick 2
