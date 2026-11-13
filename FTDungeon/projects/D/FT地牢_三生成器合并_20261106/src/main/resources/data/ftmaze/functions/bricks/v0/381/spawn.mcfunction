scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1077 211 198 minecraft:air if block 1077 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1077 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1067 211 202 minecraft:air if block 1067 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1067 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1082 211 202 minecraft:air if block 1082 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1082 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1070 211 198 minecraft:air if block 1070 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1070 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1074 211 202 minecraft:air if block 1074 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1074 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1071 211 201 minecraft:air if block 1071 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1071 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1073 211 199 minecraft:air if block 1073 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1073 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1078 211 201 minecraft:air if block 1078 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1078 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1078 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1078 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b381 ftm_brick 2
