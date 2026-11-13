scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1209 153 34 minecraft:air if block 1209 154 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1209 153 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1226 153 24 minecraft:air if block 1226 154 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1226 153 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1222 153 44 minecraft:air if block 1222 154 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1222 153 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1206 153 24 minecraft:air if block 1206 154 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1206 153 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1206 153 44 minecraft:air if block 1206 154 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1206 153 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1216 153 27 minecraft:air if block 1216 154 27 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1216 153 27 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1223 153 33 minecraft:air if block 1223 154 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1223 153 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1214 153 40 minecraft:air if block 1214 154 40 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1214 153 40 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1210 153 34 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1210 153 34 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b215 ftm_brick 2
