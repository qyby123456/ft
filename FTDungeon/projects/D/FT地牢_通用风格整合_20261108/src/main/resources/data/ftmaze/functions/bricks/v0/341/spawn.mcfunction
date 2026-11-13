scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 525 151 198 minecraft:air if block 525 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 525 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 515 151 202 minecraft:air if block 515 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 515 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 530 151 202 minecraft:air if block 530 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 530 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 518 151 198 minecraft:air if block 518 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 518 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 522 151 202 minecraft:air if block 522 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 522 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 519 151 201 minecraft:air if block 519 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 519 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 521 151 199 minecraft:air if block 521 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 521 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 526 151 201 minecraft:air if block 526 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 526 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 526 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 526 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b341 ftm_brick 2
