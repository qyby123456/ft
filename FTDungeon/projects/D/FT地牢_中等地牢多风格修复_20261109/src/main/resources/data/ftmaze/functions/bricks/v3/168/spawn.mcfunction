scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 441 211 117 minecraft:air if block 441 212 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 441 211 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 458 211 107 minecraft:air if block 458 212 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 458 211 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 454 211 127 minecraft:air if block 454 212 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 454 211 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 438 211 107 minecraft:air if block 438 212 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 438 211 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 438 211 127 minecraft:air if block 438 212 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 438 211 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 448 211 110 minecraft:air if block 448 212 110 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 448 211 110 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 455 211 116 minecraft:air if block 455 212 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 455 211 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 446 211 123 minecraft:air if block 446 212 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 446 211 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 442 211 117 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 442 211 117 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b168 ftm_brick 2
