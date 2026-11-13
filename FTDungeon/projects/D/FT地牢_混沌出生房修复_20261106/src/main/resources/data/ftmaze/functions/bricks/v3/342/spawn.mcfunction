scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 517 211 198 minecraft:air if block 517 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 517 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 507 211 202 minecraft:air if block 507 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 507 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 522 211 202 minecraft:air if block 522 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 522 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 510 211 198 minecraft:air if block 510 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 510 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 514 211 202 minecraft:air if block 514 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 514 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 511 211 201 minecraft:air if block 511 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 511 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 513 211 199 minecraft:air if block 513 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 513 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 518 211 201 minecraft:air if block 518 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 518 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 518 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 518 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b342 ftm_brick 2
