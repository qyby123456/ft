scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1311 31 198 minecraft:air if block 1311 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1311 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1301 31 202 minecraft:air if block 1301 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1301 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1316 31 202 minecraft:air if block 1316 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1316 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1304 31 198 minecraft:air if block 1304 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1304 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1308 31 202 minecraft:air if block 1308 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1308 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1305 31 201 minecraft:air if block 1305 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1305 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1307 31 199 minecraft:air if block 1307 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1307 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1312 31 201 minecraft:air if block 1312 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1312 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1312 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1312 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b400 ftm_brick 2
