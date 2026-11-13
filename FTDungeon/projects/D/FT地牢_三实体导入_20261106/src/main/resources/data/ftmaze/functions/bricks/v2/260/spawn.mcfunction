scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 231 151 304 minecraft:air if block 231 152 304 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 231 151 304 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 240 151 289 minecraft:air if block 240 152 289 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 240 151 289 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 243 151 301 minecraft:air if block 243 152 301 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 243 151 301 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 231 151 292 minecraft:air if block 231 152 292 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 231 151 292 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 232 151 304 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 232 151 304 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b260 ftm_brick 2
