scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1444 91 30 minecraft:air if block 1444 92 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1444 91 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1461 91 44 minecraft:air if block 1461 92 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1461 91 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1459 91 25 minecraft:air if block 1459 92 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1459 91 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1441 91 44 minecraft:air if block 1441 92 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1441 91 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1451 91 38 minecraft:air if block 1451 92 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1451 91 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1449 91 25 minecraft:air if block 1449 92 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1449 91 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1454 91 30 minecraft:air if block 1454 92 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1454 91 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1459 91 35 minecraft:air if block 1459 92 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1459 91 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1445 91 30 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1445 91 30 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b226 ftm_brick 2
