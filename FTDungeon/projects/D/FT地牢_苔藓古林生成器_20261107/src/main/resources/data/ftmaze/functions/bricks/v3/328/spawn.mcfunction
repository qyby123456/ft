scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 540 151 198 minecraft:air if block 540 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 540 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 530 151 202 minecraft:air if block 530 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 530 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 545 151 202 minecraft:air if block 545 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 545 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 533 151 198 minecraft:air if block 533 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 533 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 537 151 202 minecraft:air if block 537 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 537 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 534 151 201 minecraft:air if block 534 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 534 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 536 151 199 minecraft:air if block 536 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 536 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 541 151 201 minecraft:air if block 541 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 541 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 541 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 541 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b328 ftm_brick 2
