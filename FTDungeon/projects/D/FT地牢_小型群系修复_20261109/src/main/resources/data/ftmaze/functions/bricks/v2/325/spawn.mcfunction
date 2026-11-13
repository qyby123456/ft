scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 372 31 198 minecraft:air if block 372 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 372 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 362 31 202 minecraft:air if block 362 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 362 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 377 31 202 minecraft:air if block 377 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 377 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 365 31 198 minecraft:air if block 365 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 365 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 369 31 202 minecraft:air if block 369 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 369 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 366 31 201 minecraft:air if block 366 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 366 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 368 31 199 minecraft:air if block 368 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 368 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 373 31 201 minecraft:air if block 373 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 373 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 373 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 373 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b325 ftm_brick 2
