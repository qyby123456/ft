scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 233 151 305 minecraft:air if block 233 152 305 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 233 151 305 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 241 151 289 minecraft:air if block 241 152 289 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 241 151 289 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 232 151 293 minecraft:air if block 232 152 293 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 232 151 293 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 244 151 299 minecraft:air if block 244 152 299 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 244 151 299 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 233 151 304 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 233 151 304 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b259 ftm_brick 2
