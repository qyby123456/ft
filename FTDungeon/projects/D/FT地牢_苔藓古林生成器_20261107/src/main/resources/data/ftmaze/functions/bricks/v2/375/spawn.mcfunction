scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1305 211 198 minecraft:air if block 1305 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1305 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1295 211 202 minecraft:air if block 1295 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1295 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1310 211 202 minecraft:air if block 1310 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1310 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1298 211 198 minecraft:air if block 1298 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1298 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1302 211 202 minecraft:air if block 1302 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1302 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1299 211 201 minecraft:air if block 1299 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1299 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1301 211 199 minecraft:air if block 1301 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1301 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1306 211 201 minecraft:air if block 1306 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1306 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1306 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1306 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b375 ftm_brick 2
