scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 79 91 69 minecraft:air if block 79 92 69 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 79 91 69 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 90 91 56 minecraft:air if block 90 92 56 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 90 91 56 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 91 57 minecraft:air if block 70 92 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 70 91 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 81 91 59 minecraft:air if block 81 92 59 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 81 91 59 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 80 91 69 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 80 91 69 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b131 ftm_brick 2
