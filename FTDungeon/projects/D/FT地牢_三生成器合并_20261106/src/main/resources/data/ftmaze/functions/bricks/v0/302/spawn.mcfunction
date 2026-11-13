scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 430 151 198 minecraft:air if block 430 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 430 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 422 151 201 minecraft:air if block 422 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 422 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 434 151 203 minecraft:air if block 434 152 203 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 434 151 203 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 424 151 197 minecraft:air if block 424 152 197 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 424 151 197 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 430 151 197 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 430 151 197 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b302 ftm_brick 2
