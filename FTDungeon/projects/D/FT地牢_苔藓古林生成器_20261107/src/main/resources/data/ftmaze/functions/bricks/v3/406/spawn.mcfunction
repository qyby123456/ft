scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1563 151 198 minecraft:air if block 1563 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1563 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1553 151 202 minecraft:air if block 1553 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1553 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1568 151 202 minecraft:air if block 1568 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1568 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1556 151 198 minecraft:air if block 1556 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1556 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1560 151 202 minecraft:air if block 1560 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1560 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1557 151 201 minecraft:air if block 1557 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1557 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1559 151 199 minecraft:air if block 1559 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1559 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1564 151 201 minecraft:air if block 1564 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1564 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1564 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1564 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b406 ftm_brick 2
