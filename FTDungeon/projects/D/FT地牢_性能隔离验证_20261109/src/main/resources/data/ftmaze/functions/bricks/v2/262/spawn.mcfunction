scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 245 175 211 minecraft:air if block 245 176 211 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 245 175 211 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 235 175 225 minecraft:air if block 235 176 225 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 235 175 225 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 246 175 223 minecraft:air if block 246 176 223 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 246 175 223 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 237 175 215 minecraft:air if block 237 176 215 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 237 175 215 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 245 175 212 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 245 175 212 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b262 ftm_brick 2
