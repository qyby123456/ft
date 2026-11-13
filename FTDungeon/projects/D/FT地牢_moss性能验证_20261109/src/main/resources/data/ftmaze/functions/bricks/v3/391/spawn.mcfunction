scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1497 91 198 minecraft:air if block 1497 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1497 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1487 91 202 minecraft:air if block 1487 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1487 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1502 91 202 minecraft:air if block 1502 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1502 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1490 91 198 minecraft:air if block 1490 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1490 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1494 91 202 minecraft:air if block 1494 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1494 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1491 91 201 minecraft:air if block 1491 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1491 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1493 91 199 minecraft:air if block 1493 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1493 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1498 91 201 minecraft:air if block 1498 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1498 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1498 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1498 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b391 ftm_brick 2
