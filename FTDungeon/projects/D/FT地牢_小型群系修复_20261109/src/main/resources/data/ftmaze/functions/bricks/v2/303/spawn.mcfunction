scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 330 211 198 minecraft:air if block 330 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 330 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 320 211 202 minecraft:air if block 320 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 320 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 335 211 202 minecraft:air if block 335 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 335 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 323 211 198 minecraft:air if block 323 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 323 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 327 211 202 minecraft:air if block 327 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 327 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 324 211 201 minecraft:air if block 324 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 324 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 326 211 199 minecraft:air if block 326 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 326 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 331 211 201 minecraft:air if block 331 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 331 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 331 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 331 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b303 ftm_brick 2
