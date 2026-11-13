scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 238 55 223 minecraft:air if block 238 56 223 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 238 55 223 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 247 55 215 minecraft:air if block 247 56 215 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 247 55 215 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 244 55 226 minecraft:air if block 244 56 226 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 244 55 226 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 240 55 217 minecraft:air if block 240 56 217 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 240 55 217 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 238 55 224 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 238 55 224 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b255 ftm_brick 2
