scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 685 211 198 minecraft:air if block 685 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 685 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 675 211 202 minecraft:air if block 675 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 675 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 690 211 202 minecraft:air if block 690 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 690 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 678 211 198 minecraft:air if block 678 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 678 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 682 211 202 minecraft:air if block 682 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 682 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 679 211 201 minecraft:air if block 679 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 679 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 681 211 199 minecraft:air if block 681 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 681 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 686 211 201 minecraft:air if block 686 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 686 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 686 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 686 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b346 ftm_brick 2
