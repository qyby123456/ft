scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 441 31 117 minecraft:air if block 441 32 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 441 31 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 458 31 107 minecraft:air if block 458 32 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 458 31 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 454 31 127 minecraft:air if block 454 32 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 454 31 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 438 31 107 minecraft:air if block 438 32 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 438 31 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 438 31 127 minecraft:air if block 438 32 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 438 31 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 448 31 110 minecraft:air if block 448 32 110 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 448 31 110 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 455 31 116 minecraft:air if block 455 32 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 455 31 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 446 31 123 minecraft:air if block 446 32 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 446 31 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 442 31 117 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 442 31 117 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b168 ftm_brick 2
