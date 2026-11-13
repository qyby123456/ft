scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 708 211 198 minecraft:air if block 708 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 708 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 698 211 202 minecraft:air if block 698 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 698 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 713 211 202 minecraft:air if block 713 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 713 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 701 211 198 minecraft:air if block 701 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 701 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 705 211 202 minecraft:air if block 705 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 705 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 702 211 201 minecraft:air if block 702 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 702 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 704 211 199 minecraft:air if block 704 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 704 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 709 211 201 minecraft:air if block 709 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 709 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 709 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 709 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b320 ftm_brick 2
