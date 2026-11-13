scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 232 43 244 minecraft:air if block 232 44 244 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 232 43 244 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 246 43 254 minecraft:air if block 246 44 254 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 246 43 254 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 244 43 243 minecraft:air if block 244 44 243 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 244 43 243 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 236 43 252 minecraft:air if block 236 44 252 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 236 43 252 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 233 43 244 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 233 43 244 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b254 ftm_brick 2
