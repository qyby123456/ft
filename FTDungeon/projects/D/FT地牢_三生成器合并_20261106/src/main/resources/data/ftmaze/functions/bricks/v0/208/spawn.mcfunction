scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1209 151 117 minecraft:air if block 1209 152 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1209 151 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1226 151 107 minecraft:air if block 1226 152 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1226 151 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1222 151 127 minecraft:air if block 1222 152 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1222 151 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1206 151 107 minecraft:air if block 1206 152 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1206 151 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1206 151 127 minecraft:air if block 1206 152 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1206 151 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1216 151 110 minecraft:air if block 1216 152 110 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1216 151 110 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1223 151 116 minecraft:air if block 1223 152 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1223 151 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1214 151 123 minecraft:air if block 1214 152 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1214 151 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1210 151 117 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1210 151 117 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b208 ftm_brick 2
