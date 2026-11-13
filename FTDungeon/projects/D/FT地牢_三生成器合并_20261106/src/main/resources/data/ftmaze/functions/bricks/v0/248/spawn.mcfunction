scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 71 91 291 minecraft:air if block 71 92 291 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 71 91 291 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 80 91 301 minecraft:air if block 80 92 301 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 80 91 301 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 72 91 299 minecraft:air if block 72 92 299 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 72 91 299 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 80 91 293 minecraft:air if block 80 92 293 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 80 91 293 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 71 91 292 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 71 91 292 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b248 ftm_brick 2
