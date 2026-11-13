scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 643 211 198 minecraft:air if block 643 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 643 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 633 211 202 minecraft:air if block 633 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 633 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 648 211 202 minecraft:air if block 648 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 648 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 636 211 198 minecraft:air if block 636 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 636 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 640 211 202 minecraft:air if block 640 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 640 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 637 211 201 minecraft:air if block 637 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 637 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 639 211 199 minecraft:air if block 639 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 639 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 644 211 201 minecraft:air if block 644 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 644 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 644 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 644 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b345 ftm_brick 2
