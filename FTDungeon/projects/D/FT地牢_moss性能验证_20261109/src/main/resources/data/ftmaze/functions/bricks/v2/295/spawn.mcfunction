scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 236 91 293 minecraft:air if block 236 92 293 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 236 91 293 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 229 91 303 minecraft:air if block 229 92 303 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 229 91 303 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 241 91 301 minecraft:air if block 241 92 301 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 241 91 301 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 228 91 295 minecraft:air if block 228 92 295 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 228 91 295 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 237 91 293 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 237 91 293 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b295 ftm_brick 2
