scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 441 33 34 minecraft:air if block 441 34 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 441 33 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 458 33 24 minecraft:air if block 458 34 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 458 33 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 454 33 44 minecraft:air if block 454 34 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 454 33 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 438 33 24 minecraft:air if block 438 34 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 438 33 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 438 33 44 minecraft:air if block 438 34 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 438 33 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 448 33 27 minecraft:air if block 448 34 27 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 448 33 27 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 455 33 33 minecraft:air if block 455 34 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 455 33 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 446 33 40 minecraft:air if block 446 34 40 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 446 33 40 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 442 33 34 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 442 33 34 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b175 ftm_brick 2
