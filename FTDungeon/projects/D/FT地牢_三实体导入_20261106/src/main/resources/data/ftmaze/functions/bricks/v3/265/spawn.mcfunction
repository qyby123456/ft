scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 258 103 223 minecraft:air if block 258 104 223 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 258 103 223 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 267 103 215 minecraft:air if block 267 104 215 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 267 103 215 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 264 103 226 minecraft:air if block 264 104 226 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 264 103 226 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 260 103 217 minecraft:air if block 260 104 217 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 260 103 217 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 258 103 224 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 258 103 224 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b265 ftm_brick 2
