scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 582 211 198 minecraft:air if block 582 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 582 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 572 211 202 minecraft:air if block 572 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 572 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 587 211 202 minecraft:air if block 587 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 587 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 575 211 198 minecraft:air if block 575 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 575 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 579 211 202 minecraft:air if block 579 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 579 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 576 211 201 minecraft:air if block 576 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 576 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 578 211 199 minecraft:air if block 578 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 578 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 583 211 201 minecraft:air if block 583 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 583 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 583 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 583 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b317 ftm_brick 2
