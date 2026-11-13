scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 265 127 211 minecraft:air if block 265 128 211 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 265 127 211 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 255 127 225 minecraft:air if block 255 128 225 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 255 127 225 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 266 127 223 minecraft:air if block 266 128 223 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 266 127 223 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 257 127 215 minecraft:air if block 257 128 215 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 257 127 215 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 265 127 212 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 265 127 212 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b268 ftm_brick 2
