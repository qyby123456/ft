scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 253 91 305 minecraft:air if block 253 92 305 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 253 91 305 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 261 91 289 minecraft:air if block 261 92 289 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 261 91 289 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 252 91 293 minecraft:air if block 252 92 293 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 252 91 293 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 264 91 299 minecraft:air if block 264 92 299 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 264 91 299 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 253 91 304 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 253 91 304 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b264 ftm_brick 2
