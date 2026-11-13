scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1137 211 198 minecraft:air if block 1137 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1137 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1127 211 202 minecraft:air if block 1127 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1127 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1142 211 202 minecraft:air if block 1142 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1142 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1130 211 198 minecraft:air if block 1130 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1130 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1134 211 202 minecraft:air if block 1134 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1134 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1131 211 201 minecraft:air if block 1131 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1131 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1133 211 199 minecraft:air if block 1133 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1133 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1138 211 201 minecraft:air if block 1138 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1138 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1138 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1138 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b371 ftm_brick 2
