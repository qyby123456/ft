scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1305 31 198 minecraft:air if block 1305 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1305 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1295 31 202 minecraft:air if block 1295 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1295 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1310 31 202 minecraft:air if block 1310 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1310 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1298 31 198 minecraft:air if block 1298 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1298 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1302 31 202 minecraft:air if block 1302 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1302 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1299 31 201 minecraft:air if block 1299 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1299 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1301 31 199 minecraft:air if block 1301 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1301 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1306 31 201 minecraft:air if block 1306 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1306 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1306 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1306 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b374 ftm_brick 2
