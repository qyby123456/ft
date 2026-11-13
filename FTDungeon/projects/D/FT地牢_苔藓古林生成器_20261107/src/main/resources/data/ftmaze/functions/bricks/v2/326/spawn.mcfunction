scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 414 31 198 minecraft:air if block 414 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 414 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 404 31 202 minecraft:air if block 404 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 404 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 419 31 202 minecraft:air if block 419 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 419 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 407 31 198 minecraft:air if block 407 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 407 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 411 31 202 minecraft:air if block 411 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 411 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 408 31 201 minecraft:air if block 408 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 408 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 410 31 199 minecraft:air if block 410 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 410 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 415 31 201 minecraft:air if block 415 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 415 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 415 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 415 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b326 ftm_brick 2
