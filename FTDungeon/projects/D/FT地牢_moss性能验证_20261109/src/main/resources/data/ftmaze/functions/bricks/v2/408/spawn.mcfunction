scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1563 31 198 minecraft:air if block 1563 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1563 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1553 31 202 minecraft:air if block 1553 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1553 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1568 31 202 minecraft:air if block 1568 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1568 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1556 31 198 minecraft:air if block 1556 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1556 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1560 31 202 minecraft:air if block 1560 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1560 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1557 31 201 minecraft:air if block 1557 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1557 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1559 31 199 minecraft:air if block 1559 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1559 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1564 31 201 minecraft:air if block 1564 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1564 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1564 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1564 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b408 ftm_brick 2
