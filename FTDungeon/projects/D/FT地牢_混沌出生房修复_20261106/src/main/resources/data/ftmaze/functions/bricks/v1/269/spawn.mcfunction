scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 252 127 243 minecraft:air if block 252 128 243 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 252 127 243 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 266 127 253 minecraft:air if block 266 128 253 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 266 127 253 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 254 127 254 minecraft:air if block 254 128 254 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 254 127 254 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 264 127 242 minecraft:air if block 264 128 242 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 264 127 242 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 253 127 243 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 253 127 243 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b269 ftm_brick 2
