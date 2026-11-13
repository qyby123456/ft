scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 210 211 186 minecraft:air if block 210 212 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 210 211 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 221 211 183 minecraft:air if block 221 212 183 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 221 211 183 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 206 211 194 minecraft:air if block 206 212 194 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 206 211 194 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 215 211 192 minecraft:air if block 215 212 192 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 215 211 192 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 203 211 183 minecraft:air if block 203 212 183 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 203 211 183 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 214 211 182 minecraft:air if block 214 212 182 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 214 211 182 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 209 211 191 minecraft:air if block 209 212 191 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 209 211 191 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 217 211 185 minecraft:air if block 217 212 185 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 217 211 185 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 211 211 186 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 211 211 186 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b23 ftm_brick 2
