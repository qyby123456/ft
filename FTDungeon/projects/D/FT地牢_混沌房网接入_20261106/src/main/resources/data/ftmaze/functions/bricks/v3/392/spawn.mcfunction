scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1539 91 198 minecraft:air if block 1539 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1539 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1529 91 202 minecraft:air if block 1529 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1529 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1544 91 202 minecraft:air if block 1544 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1544 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1532 91 198 minecraft:air if block 1532 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1532 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1536 91 202 minecraft:air if block 1536 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1536 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1533 91 201 minecraft:air if block 1533 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1533 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1535 91 199 minecraft:air if block 1535 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1535 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1540 91 201 minecraft:air if block 1540 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1540 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1540 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1540 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b392 ftm_brick 2
