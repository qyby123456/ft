scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 594 211 55 minecraft:air if block 594 212 55 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 594 211 55 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 607 211 70 minecraft:air if block 607 212 70 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 607 211 70 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 587 211 69 minecraft:air if block 587 212 69 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 587 211 69 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 598 211 65 minecraft:air if block 598 212 65 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 598 211 65 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 606 211 57 minecraft:air if block 606 212 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 606 211 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 587 211 58 minecraft:air if block 587 212 58 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 587 211 58 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 594 211 72 minecraft:air if block 594 212 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 594 211 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 601 211 52 minecraft:air if block 601 212 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 601 211 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 594 211 56 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 594 211 56 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b181 ftm_brick 2
