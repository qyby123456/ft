scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 372 151 198 minecraft:air if block 372 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 372 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 362 151 202 minecraft:air if block 362 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 362 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 377 151 202 minecraft:air if block 377 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 377 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 365 151 198 minecraft:air if block 365 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 365 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 369 151 202 minecraft:air if block 369 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 369 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 366 151 201 minecraft:air if block 366 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 366 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 368 151 199 minecraft:air if block 368 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 368 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 373 151 201 minecraft:air if block 373 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 373 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 373 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 373 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b313 ftm_brick 2
