scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 372 211 198 minecraft:air if block 372 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 372 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 362 211 202 minecraft:air if block 362 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 362 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 377 211 202 minecraft:air if block 377 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 377 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 365 211 198 minecraft:air if block 365 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 365 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 369 211 202 minecraft:air if block 369 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 369 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 366 211 201 minecraft:air if block 366 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 366 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 368 211 199 minecraft:air if block 368 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 368 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 373 211 201 minecraft:air if block 373 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 373 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 373 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 373 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b328 ftm_brick 2
