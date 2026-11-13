scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 251 91 304 minecraft:air if block 251 92 304 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 251 91 304 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 260 91 289 minecraft:air if block 260 92 289 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 260 91 289 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 263 91 301 minecraft:air if block 263 92 301 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 263 91 301 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 251 91 292 minecraft:air if block 251 92 292 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 251 91 292 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 252 91 304 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 252 91 304 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b265 ftm_brick 2
