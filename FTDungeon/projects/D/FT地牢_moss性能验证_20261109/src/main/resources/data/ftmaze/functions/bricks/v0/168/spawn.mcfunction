scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 449 151 100 minecraft:air if block 449 152 100 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 449 151 100 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 466 151 115 minecraft:air if block 466 152 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 466 151 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 465 151 95 minecraft:air if block 465 152 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 465 151 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 446 151 115 minecraft:air if block 446 152 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 446 151 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 456 151 109 minecraft:air if block 456 152 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 456 151 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 459 151 101 minecraft:air if block 459 152 101 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 459 151 101 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 454 151 95 minecraft:air if block 454 152 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 454 151 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 464 151 106 minecraft:air if block 464 152 106 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 464 151 106 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 450 151 100 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 450 151 100 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b168 ftm_brick 2
