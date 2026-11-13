scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 252 103 244 minecraft:air if block 252 104 244 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 252 103 244 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 266 103 254 minecraft:air if block 266 104 254 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 266 103 254 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 264 103 243 minecraft:air if block 264 104 243 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 264 103 243 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 256 103 252 minecraft:air if block 256 104 252 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 256 103 252 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 253 103 244 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 253 103 244 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b266 ftm_brick 2
