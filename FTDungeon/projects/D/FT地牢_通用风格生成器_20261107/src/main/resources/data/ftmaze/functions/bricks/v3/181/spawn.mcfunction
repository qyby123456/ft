scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 594 31 55 minecraft:air if block 594 32 55 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 594 31 55 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 607 31 70 minecraft:air if block 607 32 70 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 607 31 70 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 587 31 69 minecraft:air if block 587 32 69 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 587 31 69 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 598 31 65 minecraft:air if block 598 32 65 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 598 31 65 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 606 31 57 minecraft:air if block 606 32 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 606 31 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 587 31 58 minecraft:air if block 587 32 58 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 587 31 58 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 594 31 72 minecraft:air if block 594 32 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 594 31 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 601 31 52 minecraft:air if block 601 32 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 601 31 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 594 31 56 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 594 31 56 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b181 ftm_brick 2
