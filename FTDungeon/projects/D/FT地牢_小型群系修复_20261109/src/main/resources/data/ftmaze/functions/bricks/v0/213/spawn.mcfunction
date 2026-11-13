scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1252 151 72 minecraft:air if block 1252 152 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1252 151 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1269 151 86 minecraft:air if block 1269 152 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1269 151 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1267 151 66 minecraft:air if block 1267 152 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1267 151 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1249 151 86 minecraft:air if block 1249 152 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1249 151 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1259 151 80 minecraft:air if block 1259 152 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1259 151 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1263 151 73 minecraft:air if block 1263 152 73 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1263 151 73 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1256 151 66 minecraft:air if block 1256 152 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1256 151 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1267 151 78 minecraft:air if block 1267 152 78 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1267 151 78 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1253 151 72 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1253 151 72 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b213 ftm_brick 2
