scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 441 31 71 minecraft:air if block 441 32 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 441 31 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 458 31 86 minecraft:air if block 458 32 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 458 31 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 457 31 66 minecraft:air if block 457 32 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 457 31 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 438 31 86 minecraft:air if block 438 32 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 438 31 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 448 31 80 minecraft:air if block 448 32 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 448 31 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 451 31 72 minecraft:air if block 451 32 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 451 31 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 446 31 66 minecraft:air if block 446 32 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 446 31 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 456 31 77 minecraft:air if block 456 32 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 456 31 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 442 31 71 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 442 31 71 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b172 ftm_brick 2
