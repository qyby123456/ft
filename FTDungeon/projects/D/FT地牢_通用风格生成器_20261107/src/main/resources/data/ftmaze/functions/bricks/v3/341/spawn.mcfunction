scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 475 211 198 minecraft:air if block 475 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 475 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 465 211 202 minecraft:air if block 465 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 465 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 480 211 202 minecraft:air if block 480 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 480 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 468 211 198 minecraft:air if block 468 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 468 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 472 211 202 minecraft:air if block 472 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 472 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 469 211 201 minecraft:air if block 469 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 469 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 471 211 199 minecraft:air if block 471 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 471 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 476 211 201 minecraft:air if block 476 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 476 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 476 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 476 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b341 ftm_brick 2
