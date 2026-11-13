scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 205 151 149 minecraft:air if block 205 152 149 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 205 151 149 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 218 151 162 minecraft:air if block 218 152 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 218 151 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 219 151 149 minecraft:air if block 219 152 149 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 219 151 149 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 205 151 162 minecraft:air if block 205 152 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 205 151 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 206 151 149 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 206 151 149 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b46 ftm_brick 2
