scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1221 31 198 minecraft:air if block 1221 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1221 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1211 31 202 minecraft:air if block 1211 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1211 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1226 31 202 minecraft:air if block 1226 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1226 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1214 31 198 minecraft:air if block 1214 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1214 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1218 31 202 minecraft:air if block 1218 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1218 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1215 31 201 minecraft:air if block 1215 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1215 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1217 31 199 minecraft:air if block 1217 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1217 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1222 31 201 minecraft:air if block 1222 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1222 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1222 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1222 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b372 ftm_brick 2
