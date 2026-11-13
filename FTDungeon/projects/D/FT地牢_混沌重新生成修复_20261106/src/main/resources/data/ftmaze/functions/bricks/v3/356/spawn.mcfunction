scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1105 211 198 minecraft:air if block 1105 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1105 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1095 211 202 minecraft:air if block 1095 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1095 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1110 211 202 minecraft:air if block 1110 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1110 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1098 211 198 minecraft:air if block 1098 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1098 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1102 211 202 minecraft:air if block 1102 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1102 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1099 211 201 minecraft:air if block 1099 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1099 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1101 211 199 minecraft:air if block 1101 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1101 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1106 211 201 minecraft:air if block 1106 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1106 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1106 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1106 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b356 ftm_brick 2
