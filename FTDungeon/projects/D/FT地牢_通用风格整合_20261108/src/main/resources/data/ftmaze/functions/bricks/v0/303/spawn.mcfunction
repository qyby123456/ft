scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 454 151 167 minecraft:air if block 454 152 167 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 454 151 167 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 457 151 159 minecraft:air if block 457 152 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 457 151 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 459 151 171 minecraft:air if block 459 152 171 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 459 151 171 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 453 151 161 minecraft:air if block 453 152 161 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 453 151 161 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 453 151 167 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 453 151 167 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b303 ftm_brick 2
