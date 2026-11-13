scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1563 211 198 minecraft:air if block 1563 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1563 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1553 211 202 minecraft:air if block 1553 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1553 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1568 211 202 minecraft:air if block 1568 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1568 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1556 211 198 minecraft:air if block 1556 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1556 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1560 211 202 minecraft:air if block 1560 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1560 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1557 211 201 minecraft:air if block 1557 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1557 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1559 211 199 minecraft:air if block 1559 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1559 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1564 211 201 minecraft:air if block 1564 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1564 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1564 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1564 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b410 ftm_brick 2
