scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 66 211 177 minecraft:air if block 66 212 177 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 66 211 177 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 78 211 189 minecraft:air if block 78 212 189 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 78 211 189 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 66 211 189 minecraft:air if block 66 212 189 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 66 211 189 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 73 211 182 minecraft:air if block 73 212 182 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 73 211 182 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 66 211 178 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 66 211 178 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b3 ftm_brick 2
