scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 441 93 34 minecraft:air if block 441 94 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 441 93 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 458 93 24 minecraft:air if block 458 94 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 458 93 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 454 93 44 minecraft:air if block 454 94 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 454 93 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 438 93 24 minecraft:air if block 438 94 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 438 93 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 438 93 44 minecraft:air if block 438 94 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 438 93 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 448 93 27 minecraft:air if block 448 94 27 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 448 93 27 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 455 93 33 minecraft:air if block 455 94 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 455 93 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 446 93 40 minecraft:air if block 446 94 40 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 446 93 40 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 442 93 34 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 442 93 34 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b175 ftm_brick 2
