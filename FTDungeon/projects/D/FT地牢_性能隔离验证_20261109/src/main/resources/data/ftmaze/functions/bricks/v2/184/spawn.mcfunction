scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 590 211 24 minecraft:air if block 590 212 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 590 211 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 607 211 38 minecraft:air if block 607 212 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 607 211 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 605 211 18 minecraft:air if block 605 212 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 605 211 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 587 211 38 minecraft:air if block 587 212 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 587 211 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 597 211 32 minecraft:air if block 597 212 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 597 211 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 601 211 25 minecraft:air if block 601 212 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 601 211 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 594 211 18 minecraft:air if block 594 212 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 594 211 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 605 211 30 minecraft:air if block 605 212 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 605 211 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 591 211 24 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 591 211 24 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b184 ftm_brick 2
