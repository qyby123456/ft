scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1539 211 198 minecraft:air if block 1539 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1539 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1529 211 202 minecraft:air if block 1529 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1529 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1544 211 202 minecraft:air if block 1544 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1544 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1532 211 198 minecraft:air if block 1532 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1532 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1536 211 202 minecraft:air if block 1536 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1536 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1533 211 201 minecraft:air if block 1533 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1533 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1535 211 199 minecraft:air if block 1535 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1535 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1540 211 201 minecraft:air if block 1540 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1540 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1540 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1540 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b392 ftm_brick 2
