scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1305 91 198 minecraft:air if block 1305 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1305 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1295 91 202 minecraft:air if block 1295 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1295 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1310 91 202 minecraft:air if block 1310 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1310 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1298 91 198 minecraft:air if block 1298 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1298 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1302 91 202 minecraft:air if block 1302 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1302 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1299 91 201 minecraft:air if block 1299 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1299 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1301 91 199 minecraft:air if block 1301 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1301 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1306 91 201 minecraft:air if block 1306 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1306 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1306 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1306 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b378 ftm_brick 2
