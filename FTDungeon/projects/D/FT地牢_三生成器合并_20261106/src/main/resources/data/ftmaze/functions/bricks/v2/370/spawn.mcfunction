scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1095 211 198 minecraft:air if block 1095 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1095 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1085 211 202 minecraft:air if block 1085 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1085 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1100 211 202 minecraft:air if block 1100 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1100 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1088 211 198 minecraft:air if block 1088 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1088 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1092 211 202 minecraft:air if block 1092 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1092 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1089 211 201 minecraft:air if block 1089 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1089 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1091 211 199 minecraft:air if block 1091 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1091 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1096 211 201 minecraft:air if block 1096 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1096 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1096 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1096 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b370 ftm_brick 2
