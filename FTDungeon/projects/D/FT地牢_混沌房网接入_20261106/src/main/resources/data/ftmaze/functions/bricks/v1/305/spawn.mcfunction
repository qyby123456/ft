scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 412 31 198 minecraft:air if block 412 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 412 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 404 31 201 minecraft:air if block 404 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 404 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 416 31 203 minecraft:air if block 416 32 203 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 416 31 203 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 406 31 197 minecraft:air if block 406 32 197 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 406 31 197 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 412 31 197 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 412 31 197 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b305 ftm_brick 2
