scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1539 151 198 minecraft:air if block 1539 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1539 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1529 151 202 minecraft:air if block 1529 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1529 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1544 151 202 minecraft:air if block 1544 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1544 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1532 151 198 minecraft:air if block 1532 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1532 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1536 151 202 minecraft:air if block 1536 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1536 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1533 151 201 minecraft:air if block 1533 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1533 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1535 151 199 minecraft:air if block 1535 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1535 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1540 151 201 minecraft:air if block 1540 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1540 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1540 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1540 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b393 ftm_brick 2
