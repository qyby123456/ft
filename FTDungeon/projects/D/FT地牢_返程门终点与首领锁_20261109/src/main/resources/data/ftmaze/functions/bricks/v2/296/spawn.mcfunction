scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 237 31 270 minecraft:air if block 237 32 270 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 237 31 270 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 240 31 262 minecraft:air if block 240 32 262 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 240 31 262 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 242 31 274 minecraft:air if block 242 32 274 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 242 31 274 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 236 31 264 minecraft:air if block 236 32 264 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 236 31 264 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 236 31 270 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 236 31 270 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b296 ftm_brick 2
