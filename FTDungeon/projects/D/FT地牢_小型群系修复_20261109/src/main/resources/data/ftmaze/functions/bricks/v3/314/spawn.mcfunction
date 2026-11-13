scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 456 91 198 minecraft:air if block 456 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 456 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 446 91 202 minecraft:air if block 446 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 446 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 461 91 202 minecraft:air if block 461 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 461 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 449 91 198 minecraft:air if block 449 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 449 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 453 91 202 minecraft:air if block 453 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 453 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 450 91 201 minecraft:air if block 450 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 450 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 452 91 199 minecraft:air if block 452 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 452 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 457 91 201 minecraft:air if block 457 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 457 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 457 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 457 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b314 ftm_brick 2
