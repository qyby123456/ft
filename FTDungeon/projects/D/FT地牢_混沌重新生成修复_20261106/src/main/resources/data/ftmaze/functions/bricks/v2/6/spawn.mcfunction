scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 94 211 149 minecraft:air if block 94 212 149 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 94 211 149 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 106 211 161 minecraft:air if block 106 212 161 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 106 211 161 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 94 211 161 minecraft:air if block 94 212 161 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 94 211 161 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 101 211 154 minecraft:air if block 101 212 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 101 211 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 94 211 150 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 94 211 150 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b6 ftm_brick 2
