scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 238 163 243 minecraft:air if block 238 164 243 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 238 163 243 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 247 163 253 minecraft:air if block 247 164 253 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 247 163 253 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 239 163 251 minecraft:air if block 239 164 251 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 239 163 251 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 247 163 245 minecraft:air if block 247 164 245 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 247 163 245 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 238 163 244 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 238 163 244 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b260 ftm_brick 2
