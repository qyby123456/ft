scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1000 211 198 minecraft:air if block 1000 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1000 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 992 211 201 minecraft:air if block 992 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 992 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1004 211 203 minecraft:air if block 1004 212 203 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1004 211 203 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 994 211 197 minecraft:air if block 994 212 197 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 994 211 197 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1000 211 197 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1000 211 197 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b343 ftm_brick 2
