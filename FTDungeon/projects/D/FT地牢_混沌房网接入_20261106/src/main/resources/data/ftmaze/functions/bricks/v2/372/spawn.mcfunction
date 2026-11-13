scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1179 211 198 minecraft:air if block 1179 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1179 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1169 211 202 minecraft:air if block 1169 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1169 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1184 211 202 minecraft:air if block 1184 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1184 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1172 211 198 minecraft:air if block 1172 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1172 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1176 211 202 minecraft:air if block 1176 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1176 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1173 211 201 minecraft:air if block 1173 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1173 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1175 211 199 minecraft:air if block 1175 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1175 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1180 211 201 minecraft:air if block 1180 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1180 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1180 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1180 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b372 ftm_brick 2
