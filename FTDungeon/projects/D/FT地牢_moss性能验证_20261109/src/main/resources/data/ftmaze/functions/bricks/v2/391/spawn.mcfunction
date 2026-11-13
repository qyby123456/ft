scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1455 151 198 minecraft:air if block 1455 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1455 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1445 151 202 minecraft:air if block 1445 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1445 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1460 151 202 minecraft:air if block 1460 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1460 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1448 151 198 minecraft:air if block 1448 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1448 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1452 151 202 minecraft:air if block 1452 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1452 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1449 151 201 minecraft:air if block 1449 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1449 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1451 151 199 minecraft:air if block 1451 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1451 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1456 151 201 minecraft:air if block 1456 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1456 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1456 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1456 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b391 ftm_brick 2
