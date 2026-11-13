scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1025 215 29 minecraft:air if block 1025 216 29 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1025 215 29 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1042 215 19 minecraft:air if block 1042 216 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1042 215 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1038 215 39 minecraft:air if block 1038 216 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1038 215 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1022 215 19 minecraft:air if block 1022 216 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1022 215 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1022 215 39 minecraft:air if block 1022 216 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1022 215 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1032 215 22 minecraft:air if block 1032 216 22 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1032 215 22 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1039 215 28 minecraft:air if block 1039 216 28 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1039 215 28 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1030 215 35 minecraft:air if block 1030 216 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1030 215 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1026 215 29 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1026 215 29 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b205 ftm_brick 2
