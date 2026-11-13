scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 263 103 212 minecraft:air if block 263 104 212 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 263 103 212 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 256 103 225 minecraft:air if block 256 104 225 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 256 103 225 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 267 103 222 minecraft:air if block 267 104 222 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 267 103 222 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 255 103 215 minecraft:air if block 255 104 215 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 255 103 215 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 264 103 212 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 264 103 212 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b265 ftm_brick 2
