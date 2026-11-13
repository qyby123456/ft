scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 475 91 198 minecraft:air if block 475 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 475 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 465 91 202 minecraft:air if block 465 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 465 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 480 91 202 minecraft:air if block 480 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 480 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 468 91 198 minecraft:air if block 468 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 468 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 472 91 202 minecraft:air if block 472 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 472 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 469 91 201 minecraft:air if block 469 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 469 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 471 91 199 minecraft:air if block 471 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 471 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 476 91 201 minecraft:air if block 476 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 476 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 476 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 476 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b341 ftm_brick 2
