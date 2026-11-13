scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 65 91 292 minecraft:air if block 65 92 292 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 65 91 292 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 79 91 302 minecraft:air if block 79 92 302 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 79 91 302 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 77 91 291 minecraft:air if block 77 92 291 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 77 91 291 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 69 91 300 minecraft:air if block 69 92 300 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 69 91 300 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 66 91 292 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 66 91 292 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b248 ftm_brick 2
