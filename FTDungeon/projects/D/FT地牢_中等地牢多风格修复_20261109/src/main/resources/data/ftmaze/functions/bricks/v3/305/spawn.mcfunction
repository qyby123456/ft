scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 456 31 198 minecraft:air if block 456 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 456 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 446 31 202 minecraft:air if block 446 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 446 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 461 31 202 minecraft:air if block 461 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 461 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 449 31 198 minecraft:air if block 449 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 449 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 453 31 202 minecraft:air if block 453 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 453 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 450 31 201 minecraft:air if block 450 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 450 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 452 31 199 minecraft:air if block 452 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 452 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 457 31 201 minecraft:air if block 457 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 457 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 457 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 457 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b305 ftm_brick 2
