scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1371 91 198 minecraft:air if block 1371 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1371 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1361 91 202 minecraft:air if block 1361 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1361 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1376 91 202 minecraft:air if block 1376 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1376 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1364 91 198 minecraft:air if block 1364 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1364 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1368 91 202 minecraft:air if block 1368 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1368 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1365 91 201 minecraft:air if block 1365 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1365 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1367 91 199 minecraft:air if block 1367 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1367 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1372 91 201 minecraft:air if block 1372 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1372 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1372 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1372 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b388 ftm_brick 2
