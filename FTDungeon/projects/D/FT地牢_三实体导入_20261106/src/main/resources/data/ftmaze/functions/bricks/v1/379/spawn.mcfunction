scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1347 91 198 minecraft:air if block 1347 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1347 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1337 91 202 minecraft:air if block 1337 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1337 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1352 91 202 minecraft:air if block 1352 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1352 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1340 91 198 minecraft:air if block 1340 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1340 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1344 91 202 minecraft:air if block 1344 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1344 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1341 91 201 minecraft:air if block 1341 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1341 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1343 91 199 minecraft:air if block 1343 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1343 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1348 91 201 minecraft:air if block 1348 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1348 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1348 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1348 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b379 ftm_brick 2
