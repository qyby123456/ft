scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 21 211 99 minecraft:air if block 21 212 99 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 21 211 99 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 35 211 109 minecraft:air if block 35 212 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 35 211 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 32 211 97 minecraft:air if block 32 212 97 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 32 211 97 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 27 211 105 minecraft:air if block 27 212 105 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 27 211 105 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 22 211 99 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 22 211 99 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b139 ftm_brick 2
