scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 414 211 198 minecraft:air if block 414 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 414 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 404 211 202 minecraft:air if block 404 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 404 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 419 211 202 minecraft:air if block 419 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 419 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 407 211 198 minecraft:air if block 407 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 407 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 411 211 202 minecraft:air if block 411 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 411 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 408 211 201 minecraft:air if block 408 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 408 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 410 211 199 minecraft:air if block 410 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 410 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 415 211 201 minecraft:air if block 415 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 415 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 415 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 415 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b305 ftm_brick 2
