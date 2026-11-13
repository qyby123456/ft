scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1217 37 58 minecraft:air if block 1217 38 58 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1217 37 58 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1234 37 72 minecraft:air if block 1234 38 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1234 37 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1232 37 52 minecraft:air if block 1232 38 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1232 37 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1214 37 72 minecraft:air if block 1214 38 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1214 37 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1224 37 66 minecraft:air if block 1224 38 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1224 37 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1228 37 59 minecraft:air if block 1228 38 59 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1228 37 59 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1221 37 52 minecraft:air if block 1221 38 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1221 37 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1232 37 64 minecraft:air if block 1232 38 64 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1232 37 64 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1218 37 58 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1218 37 58 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b212 ftm_brick 2
