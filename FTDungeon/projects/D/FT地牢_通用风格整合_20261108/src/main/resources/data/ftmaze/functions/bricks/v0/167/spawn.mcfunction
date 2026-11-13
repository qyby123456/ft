scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 449 151 135 minecraft:air if block 449 152 135 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 449 151 135 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 466 151 148 minecraft:air if block 466 152 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 466 151 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 464 151 130 minecraft:air if block 464 152 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 464 151 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 446 151 148 minecraft:air if block 446 152 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 446 151 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 456 151 143 minecraft:air if block 456 152 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 456 151 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 454 151 129 minecraft:air if block 454 152 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 454 151 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 459 151 135 minecraft:air if block 459 152 135 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 459 151 135 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 464 151 140 minecraft:air if block 464 152 140 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 464 151 140 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 450 151 135 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 450 151 135 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b167 ftm_brick 2
