scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 412 151 198 minecraft:air if block 412 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 412 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 404 151 201 minecraft:air if block 404 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 404 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 416 151 203 minecraft:air if block 416 152 203 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 416 151 203 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 406 151 197 minecraft:air if block 406 152 197 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 406 151 197 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 412 151 197 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 412 151 197 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b301 ftm_brick 2
