scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 65 91 291 minecraft:air if block 65 92 291 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 65 91 291 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 79 91 301 minecraft:air if block 79 92 301 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 79 91 301 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 67 91 302 minecraft:air if block 67 92 302 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 67 91 302 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 77 91 290 minecraft:air if block 77 92 290 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 77 91 290 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 66 91 291 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 66 91 291 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b248 ftm_brick 2
