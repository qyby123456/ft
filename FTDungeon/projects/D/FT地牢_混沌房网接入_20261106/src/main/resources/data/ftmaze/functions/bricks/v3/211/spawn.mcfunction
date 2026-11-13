scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1167 211 63 minecraft:air if block 1167 212 63 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1167 211 63 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1184 211 77 minecraft:air if block 1184 212 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1184 211 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1184 211 57 minecraft:air if block 1184 212 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1184 211 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1164 211 77 minecraft:air if block 1164 212 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1164 211 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1174 211 71 minecraft:air if block 1174 212 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1174 211 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1181 211 66 minecraft:air if block 1181 212 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1181 211 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1173 211 57 minecraft:air if block 1173 212 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1173 211 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1164 211 57 minecraft:air if block 1164 212 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1164 211 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1168 211 63 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1168 211 63 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b211 ftm_brick 2
