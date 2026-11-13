scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 65 151 177 minecraft:air if block 65 152 177 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 65 151 177 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 78 151 190 minecraft:air if block 78 152 190 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 78 151 190 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 79 151 177 minecraft:air if block 79 152 177 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 79 151 177 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 65 151 190 minecraft:air if block 65 152 190 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 65 151 190 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 66 151 177 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 66 151 177 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b27 ftm_brick 2
