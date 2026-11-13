scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 590 31 24 minecraft:air if block 590 32 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 590 31 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 607 31 38 minecraft:air if block 607 32 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 607 31 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 605 31 18 minecraft:air if block 605 32 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 605 31 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 587 31 38 minecraft:air if block 587 32 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 587 31 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 597 31 32 minecraft:air if block 597 32 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 597 31 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 601 31 25 minecraft:air if block 601 32 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 601 31 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 594 31 18 minecraft:air if block 594 32 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 594 31 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 605 31 30 minecraft:air if block 605 32 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 605 31 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 591 31 24 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 591 31 24 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b184 ftm_brick 2
