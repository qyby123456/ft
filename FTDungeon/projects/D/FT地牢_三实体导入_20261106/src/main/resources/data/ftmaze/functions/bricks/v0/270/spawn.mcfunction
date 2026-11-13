scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 295 151 213 minecraft:air if block 295 152 213 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 295 151 213 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 281 151 207 minecraft:air if block 281 152 207 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 281 151 207 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 285 151 216 minecraft:air if block 285 152 216 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 285 151 216 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 292 151 204 minecraft:air if block 292 152 204 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 292 151 204 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 296 151 213 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 296 151 213 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b270 ftm_brick 2
