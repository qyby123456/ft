scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 93 211 151 minecraft:air if block 93 212 151 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 93 211 151 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 107 211 162 minecraft:air if block 107 212 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 107 211 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 105 211 149 minecraft:air if block 105 212 149 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 105 211 149 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 94 211 162 minecraft:air if block 94 212 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 94 211 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 94 211 151 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 94 211 151 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b6 ftm_brick 2
