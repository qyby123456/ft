scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 414 151 198 minecraft:air if block 414 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 414 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 404 151 202 minecraft:air if block 404 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 404 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 419 151 202 minecraft:air if block 419 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 419 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 407 151 198 minecraft:air if block 407 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 407 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 411 151 202 minecraft:air if block 411 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 411 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 408 151 201 minecraft:air if block 408 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 408 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 410 151 199 minecraft:air if block 410 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 410 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 415 151 201 minecraft:air if block 415 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 415 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 415 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 415 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b316 ftm_brick 2
