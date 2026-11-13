scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1217 97 58 minecraft:air if block 1217 98 58 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1217 97 58 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1234 97 72 minecraft:air if block 1234 98 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1234 97 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1232 97 52 minecraft:air if block 1232 98 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1232 97 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1214 97 72 minecraft:air if block 1214 98 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1214 97 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1224 97 66 minecraft:air if block 1224 98 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1224 97 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1228 97 59 minecraft:air if block 1228 98 59 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1228 97 59 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1221 97 52 minecraft:air if block 1221 98 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1221 97 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1232 97 64 minecraft:air if block 1232 98 64 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1232 97 64 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1218 97 58 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1218 97 58 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b212 ftm_brick 2
