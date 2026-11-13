scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 500 151 23 minecraft:air if block 500 152 23 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 500 151 23 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 517 151 38 minecraft:air if block 517 152 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 517 151 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 516 151 18 minecraft:air if block 516 152 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 516 151 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 497 151 38 minecraft:air if block 497 152 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 497 151 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 507 151 32 minecraft:air if block 507 152 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 507 151 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 510 151 24 minecraft:air if block 510 152 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 510 151 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 505 151 18 minecraft:air if block 505 152 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 505 151 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 515 151 29 minecraft:air if block 515 152 29 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 515 151 29 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 501 151 23 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 501 151 23 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b176 ftm_brick 2
