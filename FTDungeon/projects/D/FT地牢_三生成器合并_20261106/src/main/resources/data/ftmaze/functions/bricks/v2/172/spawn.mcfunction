scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 441 91 71 minecraft:air if block 441 92 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 441 91 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 458 91 86 minecraft:air if block 458 92 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 458 91 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 457 91 66 minecraft:air if block 457 92 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 457 91 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 438 91 86 minecraft:air if block 438 92 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 438 91 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 448 91 80 minecraft:air if block 448 92 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 448 91 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 451 91 72 minecraft:air if block 451 92 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 451 91 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 446 91 66 minecraft:air if block 446 92 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 446 91 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 456 91 77 minecraft:air if block 456 92 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 456 91 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 442 91 71 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 442 91 71 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b172 ftm_brick 2
