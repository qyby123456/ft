scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1339 91 198 minecraft:air if block 1339 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1339 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1329 91 202 minecraft:air if block 1329 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1329 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1344 91 202 minecraft:air if block 1344 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1344 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1332 91 198 minecraft:air if block 1332 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1332 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1336 91 202 minecraft:air if block 1336 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1336 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1333 91 201 minecraft:air if block 1333 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1333 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1335 91 199 minecraft:air if block 1335 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1335 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1340 91 201 minecraft:air if block 1340 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1340 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1340 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1340 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b373 ftm_brick 2
