scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1209 151 71 minecraft:air if block 1209 152 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1209 151 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1226 151 86 minecraft:air if block 1226 152 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1226 151 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1225 151 66 minecraft:air if block 1225 152 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1225 151 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1206 151 86 minecraft:air if block 1206 152 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1206 151 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1216 151 80 minecraft:air if block 1216 152 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1216 151 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1219 151 72 minecraft:air if block 1219 152 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1219 151 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1214 151 66 minecraft:air if block 1214 152 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1214 151 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1224 151 77 minecraft:air if block 1224 152 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1224 151 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1210 151 71 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1210 151 71 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b212 ftm_brick 2
