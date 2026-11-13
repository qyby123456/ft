scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1245 151 198 minecraft:air if block 1245 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1245 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1235 151 202 minecraft:air if block 1235 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1235 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1250 151 202 minecraft:air if block 1250 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1250 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1238 151 198 minecraft:air if block 1238 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1238 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1242 151 202 minecraft:air if block 1242 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1242 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1239 151 201 minecraft:air if block 1239 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1239 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1241 151 199 minecraft:air if block 1241 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1241 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1246 151 201 minecraft:air if block 1246 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1246 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1246 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1246 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b389 ftm_brick 2
