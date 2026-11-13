scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 456 151 198 minecraft:air if block 456 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 456 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 446 151 202 minecraft:air if block 446 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 446 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 461 151 202 minecraft:air if block 461 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 461 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 449 151 198 minecraft:air if block 449 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 449 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 453 151 202 minecraft:air if block 453 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 453 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 450 151 201 minecraft:air if block 450 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 450 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 452 151 199 minecraft:air if block 452 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 452 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 457 151 201 minecraft:air if block 457 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 457 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 457 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 457 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b317 ftm_brick 2
