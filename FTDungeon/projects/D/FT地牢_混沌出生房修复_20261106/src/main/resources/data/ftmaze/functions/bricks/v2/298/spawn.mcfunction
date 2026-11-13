scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 259 151 206 minecraft:air if block 259 152 206 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 259 151 206 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 251 151 209 minecraft:air if block 251 152 209 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 251 151 209 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 263 151 211 minecraft:air if block 263 152 211 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 263 151 211 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 253 151 205 minecraft:air if block 253 152 205 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 253 151 205 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 259 151 205 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 259 151 205 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b298 ftm_brick 2
