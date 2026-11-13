scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 540 31 198 minecraft:air if block 540 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 540 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 530 31 202 minecraft:air if block 530 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 530 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 545 31 202 minecraft:air if block 545 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 545 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 533 31 198 minecraft:air if block 533 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 533 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 537 31 202 minecraft:air if block 537 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 537 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 534 31 201 minecraft:air if block 534 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 534 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 536 31 199 minecraft:air if block 536 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 536 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 541 31 201 minecraft:air if block 541 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 541 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 541 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 541 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b328 ftm_brick 2
