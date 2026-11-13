scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 206 211 152 minecraft:air if block 206 212 152 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 206 211 152 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 217 211 161 minecraft:air if block 217 212 161 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 217 211 161 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 216 211 150 minecraft:air if block 216 212 150 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 216 211 150 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 207 211 161 minecraft:air if block 207 212 161 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 207 211 161 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 206 211 151 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 206 211 151 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b22 ftm_brick 2
