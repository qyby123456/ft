scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 107 151 66 minecraft:air if block 107 152 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 107 151 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 96 151 59 minecraft:air if block 96 152 59 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 96 151 59 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 98 151 73 minecraft:air if block 98 152 73 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 98 151 73 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 99 151 65 minecraft:air if block 99 152 65 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 99 151 65 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 108 151 66 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 108 151 66 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b115 ftm_brick 2
