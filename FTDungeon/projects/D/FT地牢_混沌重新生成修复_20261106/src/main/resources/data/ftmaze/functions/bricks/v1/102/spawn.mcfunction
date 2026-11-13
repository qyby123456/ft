scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 76 211 60 minecraft:air if block 76 212 60 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 76 211 60 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 88 211 74 minecraft:air if block 88 212 74 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 88 211 74 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 211 72 minecraft:air if block 70 212 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 70 211 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 90 211 61 minecraft:air if block 90 212 61 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 90 211 61 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 81 211 68 minecraft:air if block 81 212 68 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 81 211 68 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 211 63 minecraft:air if block 70 212 63 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 70 211 63 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 77 211 74 minecraft:air if block 77 212 74 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 77 211 74 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 83 211 58 minecraft:air if block 83 212 58 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 83 211 58 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 77 211 60 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 77 211 60 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b102 ftm_brick 2
