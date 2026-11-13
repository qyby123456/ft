scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1217 31 100 minecraft:air if block 1217 32 100 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1217 31 100 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1234 31 115 minecraft:air if block 1234 32 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1234 31 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1233 31 95 minecraft:air if block 1233 32 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1233 31 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1214 31 115 minecraft:air if block 1214 32 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1214 31 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1224 31 109 minecraft:air if block 1224 32 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1224 31 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1227 31 101 minecraft:air if block 1227 32 101 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1227 31 101 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1222 31 95 minecraft:air if block 1222 32 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1222 31 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1232 31 106 minecraft:air if block 1232 32 106 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1232 31 106 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1218 31 100 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1218 31 100 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b208 ftm_brick 2
