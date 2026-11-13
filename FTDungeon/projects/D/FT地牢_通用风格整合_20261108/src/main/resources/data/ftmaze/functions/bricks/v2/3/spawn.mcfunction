scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 66 211 181 minecraft:air if block 66 212 181 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 66 211 181 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 78 211 190 minecraft:air if block 78 212 190 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 78 211 190 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 76 211 177 minecraft:air if block 76 212 177 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 76 211 177 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 67 211 190 minecraft:air if block 67 212 190 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 67 211 190 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 66 211 180 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 66 211 180 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b3 ftm_brick 2
