scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 441 211 71 minecraft:air if block 441 212 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 441 211 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 458 211 86 minecraft:air if block 458 212 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 458 211 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 457 211 66 minecraft:air if block 457 212 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 457 211 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 438 211 86 minecraft:air if block 438 212 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 438 211 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 448 211 80 minecraft:air if block 448 212 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 448 211 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 451 211 72 minecraft:air if block 451 212 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 451 211 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 446 211 66 minecraft:air if block 446 212 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 446 211 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 456 211 77 minecraft:air if block 456 212 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 456 211 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 442 211 71 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 442 211 71 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b172 ftm_brick 2
