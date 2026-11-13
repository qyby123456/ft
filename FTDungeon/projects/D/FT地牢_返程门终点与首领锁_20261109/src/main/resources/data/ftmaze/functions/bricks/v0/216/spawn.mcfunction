scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1252 151 30 minecraft:air if block 1252 152 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1252 151 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1269 151 44 minecraft:air if block 1269 152 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1269 151 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1267 151 25 minecraft:air if block 1267 152 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1267 151 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1249 151 44 minecraft:air if block 1249 152 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1249 151 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1259 151 38 minecraft:air if block 1259 152 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1259 151 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1257 151 25 minecraft:air if block 1257 152 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1257 151 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1262 151 30 minecraft:air if block 1262 152 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1262 151 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1267 151 35 minecraft:air if block 1267 152 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1267 151 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1253 151 30 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1253 151 30 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b216 ftm_brick 2
