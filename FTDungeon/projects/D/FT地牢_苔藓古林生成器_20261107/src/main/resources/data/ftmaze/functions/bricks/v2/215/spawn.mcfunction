scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1217 97 25 minecraft:air if block 1217 98 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1217 97 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1234 97 17 minecraft:air if block 1234 98 17 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1234 97 17 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1229 97 33 minecraft:air if block 1229 98 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1229 97 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1222 97 16 minecraft:air if block 1222 98 16 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1222 97 16 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1227 97 23 minecraft:air if block 1227 98 23 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1227 97 23 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1214 97 33 minecraft:air if block 1214 98 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1214 97 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1214 97 18 minecraft:air if block 1214 98 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1214 97 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1222 97 30 minecraft:air if block 1222 98 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1222 97 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1218 97 25 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1218 97 25 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b215 ftm_brick 2
