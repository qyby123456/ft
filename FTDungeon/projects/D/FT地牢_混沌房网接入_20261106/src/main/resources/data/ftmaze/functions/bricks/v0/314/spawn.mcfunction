scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 456 211 198 minecraft:air if block 456 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 456 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 446 211 202 minecraft:air if block 446 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 446 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 461 211 202 minecraft:air if block 461 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 461 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 449 211 198 minecraft:air if block 449 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 449 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 453 211 202 minecraft:air if block 453 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 453 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 450 211 201 minecraft:air if block 450 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 450 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 452 211 199 minecraft:air if block 452 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 452 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 457 211 201 minecraft:air if block 457 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 457 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 457 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 457 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b314 ftm_brick 2
