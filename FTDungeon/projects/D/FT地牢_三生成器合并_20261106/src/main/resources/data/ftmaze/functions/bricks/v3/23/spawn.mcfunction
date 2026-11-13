scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 217 211 177 minecraft:air if block 217 212 177 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 217 211 177 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 203 211 184 minecraft:air if block 203 212 184 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 203 211 184 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 206 211 174 minecraft:air if block 206 212 174 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 206 211 174 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 221 211 186 minecraft:air if block 221 212 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 221 211 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 210 211 180 minecraft:air if block 210 212 180 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 210 211 180 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 213 211 172 minecraft:air if block 213 212 172 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 213 211 172 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 212 211 186 minecraft:air if block 212 212 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 212 211 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 215 211 182 minecraft:air if block 215 212 182 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 215 211 182 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 218 211 177 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 218 211 177 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b23 ftm_brick 2
