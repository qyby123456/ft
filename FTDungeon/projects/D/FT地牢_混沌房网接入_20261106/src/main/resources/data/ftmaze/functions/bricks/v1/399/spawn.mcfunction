scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1101 211 198 minecraft:air if block 1101 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1101 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1091 211 202 minecraft:air if block 1091 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1091 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1106 211 202 minecraft:air if block 1106 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1106 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1094 211 198 minecraft:air if block 1094 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1094 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1098 211 202 minecraft:air if block 1098 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1098 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1095 211 201 minecraft:air if block 1095 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1095 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1097 211 199 minecraft:air if block 1097 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1097 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1102 211 201 minecraft:air if block 1102 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1102 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1102 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1102 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b399 ftm_brick 2
