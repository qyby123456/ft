scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1311 211 198 minecraft:air if block 1311 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1311 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1301 211 202 minecraft:air if block 1301 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1301 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1316 211 202 minecraft:air if block 1316 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1316 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1304 211 198 minecraft:air if block 1304 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1304 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1308 211 202 minecraft:air if block 1308 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1308 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1305 211 201 minecraft:air if block 1305 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1305 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1307 211 199 minecraft:air if block 1307 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1307 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1312 211 201 minecraft:air if block 1312 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1312 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1312 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1312 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b404 ftm_brick 2
