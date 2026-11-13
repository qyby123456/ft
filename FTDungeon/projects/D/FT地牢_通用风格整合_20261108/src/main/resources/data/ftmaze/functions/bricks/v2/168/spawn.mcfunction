scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 441 91 117 minecraft:air if block 441 92 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 441 91 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 458 91 107 minecraft:air if block 458 92 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 458 91 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 454 91 127 minecraft:air if block 454 92 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 454 91 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 438 91 107 minecraft:air if block 438 92 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 438 91 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 438 91 127 minecraft:air if block 438 92 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 438 91 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 448 91 110 minecraft:air if block 448 92 110 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 448 91 110 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 455 91 116 minecraft:air if block 455 92 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 455 91 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 446 91 123 minecraft:air if block 446 92 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 446 91 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 442 91 117 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 442 91 117 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b168 ftm_brick 2
