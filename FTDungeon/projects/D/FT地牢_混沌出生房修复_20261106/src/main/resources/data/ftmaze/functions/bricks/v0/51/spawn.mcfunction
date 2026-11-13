scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 65 91 179 minecraft:air if block 65 92 179 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 65 91 179 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 79 91 190 minecraft:air if block 79 92 190 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 79 91 190 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 77 91 177 minecraft:air if block 77 92 177 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 77 91 177 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 66 91 190 minecraft:air if block 66 92 190 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 66 91 190 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 66 91 179 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 66 91 179 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b51 ftm_brick 2
