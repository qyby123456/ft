scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1209 151 148 minecraft:air if block 1209 152 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1209 151 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1224 151 163 minecraft:air if block 1224 152 163 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1224 151 163 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1224 151 143 minecraft:air if block 1224 152 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1224 151 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1208 151 163 minecraft:air if block 1208 152 163 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1208 151 163 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1216 151 156 minecraft:air if block 1216 152 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1216 151 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1226 151 153 minecraft:air if block 1226 152 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1226 151 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1206 151 155 minecraft:air if block 1206 152 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1206 151 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1214 151 143 minecraft:air if block 1214 152 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1214 151 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1210 151 148 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1210 151 148 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b207 ftm_brick 2
