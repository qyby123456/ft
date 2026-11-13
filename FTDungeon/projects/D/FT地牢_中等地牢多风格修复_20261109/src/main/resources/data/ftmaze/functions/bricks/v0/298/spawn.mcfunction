scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 257 91 270 minecraft:air if block 257 92 270 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 257 91 270 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 260 91 262 minecraft:air if block 260 92 262 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 260 91 262 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 262 91 274 minecraft:air if block 262 92 274 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 262 91 274 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 256 91 264 minecraft:air if block 256 92 264 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 256 91 264 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 256 91 270 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 256 91 270 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b298 ftm_brick 2
