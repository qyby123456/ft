scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 412 211 198 minecraft:air if block 412 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 412 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 404 211 201 minecraft:air if block 404 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 404 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 416 211 203 minecraft:air if block 416 212 203 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 416 211 203 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 406 211 197 minecraft:air if block 406 212 197 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 406 211 197 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 412 211 197 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 412 211 197 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b301 ftm_brick 2
