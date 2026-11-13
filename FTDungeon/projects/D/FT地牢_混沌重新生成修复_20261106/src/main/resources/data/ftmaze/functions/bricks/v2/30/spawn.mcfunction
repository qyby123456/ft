scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 93 151 151 minecraft:air if block 93 152 151 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 93 151 151 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 107 151 162 minecraft:air if block 107 152 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 107 151 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 105 151 149 minecraft:air if block 105 152 149 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 105 151 149 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 94 151 162 minecraft:air if block 94 152 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 94 151 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 94 151 151 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 94 151 151 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b30 ftm_brick 2
