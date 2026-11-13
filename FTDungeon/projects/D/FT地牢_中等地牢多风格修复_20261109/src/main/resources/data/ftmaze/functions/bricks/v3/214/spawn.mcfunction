scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1168 211 25 minecraft:air if block 1168 212 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1168 211 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1181 211 38 minecraft:air if block 1181 212 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1181 211 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1182 211 19 minecraft:air if block 1182 212 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1182 211 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1164 211 35 minecraft:air if block 1164 212 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1164 211 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1174 211 32 minecraft:air if block 1174 212 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1174 211 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1184 211 29 minecraft:air if block 1184 212 29 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1184 211 29 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1171 211 18 minecraft:air if block 1171 212 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1171 211 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1176 211 23 minecraft:air if block 1176 212 23 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1176 211 23 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1168 211 24 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1168 211 24 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b214 ftm_brick 2
