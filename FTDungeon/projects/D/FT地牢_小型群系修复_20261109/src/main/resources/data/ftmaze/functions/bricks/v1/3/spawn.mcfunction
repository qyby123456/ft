scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 66 211 178 minecraft:air if block 66 212 178 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 66 211 178 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 78 211 189 minecraft:air if block 78 212 189 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 78 211 189 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 79 211 176 minecraft:air if block 79 212 176 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 79 211 176 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 64 211 189 minecraft:air if block 64 212 189 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 64 211 189 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 66 211 179 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 66 211 179 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b3 ftm_brick 2
