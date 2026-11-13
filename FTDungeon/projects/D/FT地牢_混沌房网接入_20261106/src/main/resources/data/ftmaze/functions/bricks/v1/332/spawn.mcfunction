scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 540 211 198 minecraft:air if block 540 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 540 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 530 211 202 minecraft:air if block 530 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 530 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 545 211 202 minecraft:air if block 545 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 545 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 533 211 198 minecraft:air if block 533 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 533 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 537 211 202 minecraft:air if block 537 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 537 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 534 211 201 minecraft:air if block 534 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 534 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 536 211 199 minecraft:air if block 536 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 536 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 541 211 201 minecraft:air if block 541 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 541 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 541 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 541 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b332 ftm_brick 2
