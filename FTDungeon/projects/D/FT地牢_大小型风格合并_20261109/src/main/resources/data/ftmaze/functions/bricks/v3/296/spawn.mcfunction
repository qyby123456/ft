scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 237 151 270 minecraft:air if block 237 152 270 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 237 151 270 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 240 151 262 minecraft:air if block 240 152 262 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 240 151 262 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 242 151 274 minecraft:air if block 242 152 274 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 242 151 274 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 236 151 264 minecraft:air if block 236 152 264 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 236 151 264 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 236 151 270 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 236 151 270 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b296 ftm_brick 2
