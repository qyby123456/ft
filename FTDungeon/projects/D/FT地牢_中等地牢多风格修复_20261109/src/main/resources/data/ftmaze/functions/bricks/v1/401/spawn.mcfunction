scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1185 211 198 minecraft:air if block 1185 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1185 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1175 211 202 minecraft:air if block 1175 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1175 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1190 211 202 minecraft:air if block 1190 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1190 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1178 211 198 minecraft:air if block 1178 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1178 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1182 211 202 minecraft:air if block 1182 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1182 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1179 211 201 minecraft:air if block 1179 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1179 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1181 211 199 minecraft:air if block 1181 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1181 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1186 211 201 minecraft:air if block 1186 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1186 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1186 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1186 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b401 ftm_brick 2
