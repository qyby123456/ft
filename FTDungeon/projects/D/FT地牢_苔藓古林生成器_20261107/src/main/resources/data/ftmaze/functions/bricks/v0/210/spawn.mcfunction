scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1252 151 117 minecraft:air if block 1252 152 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1252 151 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1269 151 108 minecraft:air if block 1269 152 108 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1269 151 108 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1265 151 127 minecraft:air if block 1265 152 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1265 151 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1256 151 107 minecraft:air if block 1256 152 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1256 151 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1262 151 114 minecraft:air if block 1262 152 114 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1262 151 114 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1249 151 126 minecraft:air if block 1249 152 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1249 151 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1257 151 123 minecraft:air if block 1257 152 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1257 151 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1268 151 119 minecraft:air if block 1268 152 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1268 151 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1253 151 117 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1253 151 117 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b210 ftm_brick 2
