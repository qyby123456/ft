scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1444 31 30 minecraft:air if block 1444 32 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1444 31 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1461 31 44 minecraft:air if block 1461 32 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1461 31 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1459 31 25 minecraft:air if block 1459 32 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1459 31 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1441 31 44 minecraft:air if block 1441 32 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1441 31 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1451 31 38 minecraft:air if block 1451 32 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1451 31 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1449 31 25 minecraft:air if block 1449 32 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1449 31 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1454 31 30 minecraft:air if block 1454 32 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1454 31 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1459 31 35 minecraft:air if block 1459 32 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1459 31 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1445 31 30 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1445 31 30 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b226 ftm_brick 2
