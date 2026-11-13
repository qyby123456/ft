scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 177 211 180 minecraft:air if block 177 212 180 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 177 211 180 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 191 211 189 minecraft:air if block 191 212 189 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 191 211 189 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 188 211 177 minecraft:air if block 188 212 177 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 188 211 177 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 181 211 187 minecraft:air if block 181 212 187 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 181 211 187 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 178 211 180 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 178 211 180 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b19 ftm_brick 2
