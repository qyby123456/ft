scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1497 151 198 minecraft:air if block 1497 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1497 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1487 151 202 minecraft:air if block 1487 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1487 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1502 151 202 minecraft:air if block 1502 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1502 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1490 151 198 minecraft:air if block 1490 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1490 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1494 151 202 minecraft:air if block 1494 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1494 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1491 151 201 minecraft:air if block 1491 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1491 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1493 151 199 minecraft:air if block 1493 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1493 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1498 151 201 minecraft:air if block 1498 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1498 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1498 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1498 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b395 ftm_brick 2
