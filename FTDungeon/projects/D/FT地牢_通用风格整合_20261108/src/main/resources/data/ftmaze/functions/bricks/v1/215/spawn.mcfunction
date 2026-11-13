scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1217 37 25 minecraft:air if block 1217 38 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1217 37 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1234 37 17 minecraft:air if block 1234 38 17 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1234 37 17 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1229 37 33 minecraft:air if block 1229 38 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1229 37 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1222 37 16 minecraft:air if block 1222 38 16 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1222 37 16 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1227 37 23 minecraft:air if block 1227 38 23 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1227 37 23 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1214 37 33 minecraft:air if block 1214 38 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1214 37 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1214 37 18 minecraft:air if block 1214 38 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1214 37 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1222 37 30 minecraft:air if block 1222 38 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1222 37 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1218 37 25 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1218 37 25 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b215 ftm_brick 2
