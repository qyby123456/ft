scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1455 91 198 minecraft:air if block 1455 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1455 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1445 91 202 minecraft:air if block 1445 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1445 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1460 91 202 minecraft:air if block 1460 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1460 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1448 91 198 minecraft:air if block 1448 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1448 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1452 91 202 minecraft:air if block 1452 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1452 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1449 91 201 minecraft:air if block 1449 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1449 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1451 91 199 minecraft:air if block 1451 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1451 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1456 91 201 minecraft:air if block 1456 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1456 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1456 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1456 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b390 ftm_brick 2
