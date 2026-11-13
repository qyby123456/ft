scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 402 151 55 minecraft:air if block 402 152 55 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 402 151 55 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 415 151 70 minecraft:air if block 415 152 70 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 415 151 70 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 395 151 69 minecraft:air if block 395 152 69 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 395 151 69 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 406 151 65 minecraft:air if block 406 152 65 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 406 151 65 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 414 151 57 minecraft:air if block 414 152 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 414 151 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 395 151 58 minecraft:air if block 395 152 58 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 395 151 58 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 402 151 72 minecraft:air if block 402 152 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 402 151 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 409 151 52 minecraft:air if block 409 152 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 409 151 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 402 151 56 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 402 151 56 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b171 ftm_brick 2
