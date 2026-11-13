scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 245 31 211 minecraft:air if block 245 32 211 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 245 31 211 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 235 31 225 minecraft:air if block 235 32 225 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 235 31 225 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 246 31 223 minecraft:air if block 246 32 223 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 246 31 223 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 237 31 215 minecraft:air if block 237 32 215 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 237 31 215 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 245 31 212 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 245 31 212 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b253 ftm_brick 2
