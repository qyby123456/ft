scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 601 211 198 minecraft:air if block 601 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 601 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 591 211 202 minecraft:air if block 591 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 591 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 606 211 202 minecraft:air if block 606 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 606 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 594 211 198 minecraft:air if block 594 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 594 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 598 211 202 minecraft:air if block 598 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 598 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 595 211 201 minecraft:air if block 595 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 595 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 597 211 199 minecraft:air if block 597 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 597 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 602 211 201 minecraft:air if block 602 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 602 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 602 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 602 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b344 ftm_brick 2
