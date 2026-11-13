scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 540 91 198 minecraft:air if block 540 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 540 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 530 91 202 minecraft:air if block 530 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 530 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 545 91 202 minecraft:air if block 545 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 545 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 533 91 198 minecraft:air if block 533 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 533 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 537 91 202 minecraft:air if block 537 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 537 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 534 91 201 minecraft:air if block 534 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 534 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 536 91 199 minecraft:air if block 536 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 536 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 541 91 201 minecraft:air if block 541 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 541 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 541 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 541 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b316 ftm_brick 2
