scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1311 151 198 minecraft:air if block 1311 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1311 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1301 151 202 minecraft:air if block 1301 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1301 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1316 151 202 minecraft:air if block 1316 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1316 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1304 151 198 minecraft:air if block 1304 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1304 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1308 151 202 minecraft:air if block 1308 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1308 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1305 151 201 minecraft:air if block 1305 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1305 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1307 151 199 minecraft:air if block 1307 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1307 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1312 151 201 minecraft:air if block 1312 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1312 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1312 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1312 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b400 ftm_brick 2
