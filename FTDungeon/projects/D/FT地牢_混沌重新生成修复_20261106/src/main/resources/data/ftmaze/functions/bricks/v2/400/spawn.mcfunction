scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1227 31 198 minecraft:air if block 1227 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1227 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1217 31 202 minecraft:air if block 1217 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1217 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1232 31 202 minecraft:air if block 1232 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1232 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1220 31 198 minecraft:air if block 1220 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1220 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1224 31 202 minecraft:air if block 1224 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1224 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1221 31 201 minecraft:air if block 1221 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1221 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1223 31 199 minecraft:air if block 1223 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1223 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1228 31 201 minecraft:air if block 1228 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1228 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1228 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1228 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b400 ftm_brick 2
