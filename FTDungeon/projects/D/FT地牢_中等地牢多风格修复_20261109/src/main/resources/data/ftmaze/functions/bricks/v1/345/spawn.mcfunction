scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 517 31 198 minecraft:air if block 517 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 517 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 507 31 202 minecraft:air if block 507 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 507 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 522 31 202 minecraft:air if block 522 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 522 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 510 31 198 minecraft:air if block 510 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 510 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 514 31 202 minecraft:air if block 514 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 514 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 511 31 201 minecraft:air if block 511 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 511 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 513 31 199 minecraft:air if block 513 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 513 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 518 31 201 minecraft:air if block 518 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 518 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 518 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 518 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b345 ftm_brick 2
