scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 582 151 198 minecraft:air if block 582 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 582 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 572 151 202 minecraft:air if block 572 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 572 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 587 151 202 minecraft:air if block 587 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 587 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 575 151 198 minecraft:air if block 575 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 575 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 579 151 202 minecraft:air if block 579 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 579 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 576 151 201 minecraft:air if block 576 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 576 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 578 151 199 minecraft:air if block 578 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 578 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 583 151 201 minecraft:air if block 583 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 583 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 583 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 583 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b320 ftm_brick 2
