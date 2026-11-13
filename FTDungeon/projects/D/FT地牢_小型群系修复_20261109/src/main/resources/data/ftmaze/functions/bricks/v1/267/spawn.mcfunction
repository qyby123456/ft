scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 258 103 243 minecraft:air if block 258 104 243 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 258 103 243 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 267 103 253 minecraft:air if block 267 104 253 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 267 103 253 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 259 103 251 minecraft:air if block 259 104 251 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 259 103 251 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 267 103 245 minecraft:air if block 267 104 245 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 267 103 245 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 258 103 244 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 258 103 244 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b267 ftm_brick 2
