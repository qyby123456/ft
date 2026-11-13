scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1227 151 198 minecraft:air if block 1227 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1227 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1217 151 202 minecraft:air if block 1217 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1217 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1232 151 202 minecraft:air if block 1232 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1232 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1220 151 198 minecraft:air if block 1220 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1220 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1224 151 202 minecraft:air if block 1224 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1224 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1221 151 201 minecraft:air if block 1221 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1221 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1223 151 199 minecraft:air if block 1223 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1223 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1228 151 201 minecraft:air if block 1228 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1228 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1228 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1228 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b398 ftm_brick 2
