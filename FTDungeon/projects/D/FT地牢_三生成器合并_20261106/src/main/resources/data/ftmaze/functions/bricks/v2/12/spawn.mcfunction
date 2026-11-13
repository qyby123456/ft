scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 151 211 96 minecraft:air if block 151 212 96 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 151 211 96 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 161 211 106 minecraft:air if block 161 212 106 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 161 211 106 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 162 211 96 minecraft:air if block 162 212 96 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 162 211 96 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 151 211 106 minecraft:air if block 151 212 106 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 151 211 106 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 150 211 96 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 150 211 96 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b12 ftm_brick 2
