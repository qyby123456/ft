scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 475 31 198 minecraft:air if block 475 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 475 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 465 31 202 minecraft:air if block 465 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 465 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 480 31 202 minecraft:air if block 480 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 480 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 468 31 198 minecraft:air if block 468 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 468 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 472 31 202 minecraft:air if block 472 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 472 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 469 31 201 minecraft:air if block 469 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 469 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 471 31 199 minecraft:air if block 471 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 471 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 476 31 201 minecraft:air if block 476 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 476 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 476 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 476 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b344 ftm_brick 2
