scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 218 91 293 minecraft:air if block 218 92 293 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 218 91 293 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 211 91 303 minecraft:air if block 211 92 303 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 211 91 303 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 223 91 301 minecraft:air if block 223 92 301 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 223 91 301 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 210 91 295 minecraft:air if block 210 92 295 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 210 91 295 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 219 91 293 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 219 91 293 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b293 ftm_brick 2
