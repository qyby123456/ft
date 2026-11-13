scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 275 211 213 minecraft:air if block 275 212 213 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 275 211 213 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 261 211 207 minecraft:air if block 261 212 207 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 261 211 207 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 265 211 216 minecraft:air if block 265 212 216 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 265 211 216 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 272 211 204 minecraft:air if block 272 212 204 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 272 211 204 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 276 211 213 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 276 211 213 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b270 ftm_brick 2
