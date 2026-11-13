scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1217 91 100 minecraft:air if block 1217 92 100 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1217 91 100 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1234 91 115 minecraft:air if block 1234 92 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1234 91 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1233 91 95 minecraft:air if block 1233 92 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1233 91 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1214 91 115 minecraft:air if block 1214 92 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1214 91 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1224 91 109 minecraft:air if block 1224 92 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1224 91 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1227 91 101 minecraft:air if block 1227 92 101 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1227 91 101 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1222 91 95 minecraft:air if block 1222 92 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1222 91 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1232 91 106 minecraft:air if block 1232 92 106 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1232 91 106 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1218 91 100 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1218 91 100 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b208 ftm_brick 2
