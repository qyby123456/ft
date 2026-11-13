scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 79 151 69 minecraft:air if block 79 152 69 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 79 151 69 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 90 151 56 minecraft:air if block 90 152 56 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 90 151 56 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 151 57 minecraft:air if block 70 152 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 70 151 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 81 151 59 minecraft:air if block 81 152 59 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 81 151 59 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 80 151 69 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 80 151 69 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b117 ftm_brick 2
