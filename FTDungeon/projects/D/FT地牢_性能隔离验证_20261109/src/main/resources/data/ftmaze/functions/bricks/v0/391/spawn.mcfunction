scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1497 211 198 minecraft:air if block 1497 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1497 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1487 211 202 minecraft:air if block 1487 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1487 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1502 211 202 minecraft:air if block 1502 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1502 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1490 211 198 minecraft:air if block 1490 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1490 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1494 211 202 minecraft:air if block 1494 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1494 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1491 211 201 minecraft:air if block 1491 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1491 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1493 211 199 minecraft:air if block 1493 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1493 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1498 211 201 minecraft:air if block 1498 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1498 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1498 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1498 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b391 ftm_brick 2
