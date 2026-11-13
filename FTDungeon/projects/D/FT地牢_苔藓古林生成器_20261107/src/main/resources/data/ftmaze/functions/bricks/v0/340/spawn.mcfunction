scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 483 151 198 minecraft:air if block 483 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 483 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 473 151 202 minecraft:air if block 473 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 473 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 488 151 202 minecraft:air if block 488 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 488 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 476 151 198 minecraft:air if block 476 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 476 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 480 151 202 minecraft:air if block 480 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 480 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 477 151 201 minecraft:air if block 477 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 477 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 479 151 199 minecraft:air if block 479 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 479 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 484 151 201 minecraft:air if block 484 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 484 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 484 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 484 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b340 ftm_brick 2
