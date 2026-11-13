scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1371 211 198 minecraft:air if block 1371 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1371 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1361 211 202 minecraft:air if block 1361 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1361 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1376 211 202 minecraft:air if block 1376 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1376 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1364 211 198 minecraft:air if block 1364 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1364 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1368 211 202 minecraft:air if block 1368 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1368 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1365 211 201 minecraft:air if block 1365 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1365 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1367 211 199 minecraft:air if block 1367 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1367 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1372 211 201 minecraft:air if block 1372 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1372 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1372 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1372 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b388 ftm_brick 2
