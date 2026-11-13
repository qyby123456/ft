scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 94 211 150 minecraft:air if block 94 212 150 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 94 211 150 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 106 211 161 minecraft:air if block 106 212 161 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 106 211 161 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 107 211 148 minecraft:air if block 107 212 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 107 211 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 92 211 161 minecraft:air if block 92 212 161 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 92 211 161 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 94 211 151 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 94 211 151 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b6 ftm_brick 2
