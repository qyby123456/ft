scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 283 151 207 minecraft:air if block 283 152 207 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 283 151 207 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 293 151 216 minecraft:air if block 293 152 216 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 293 151 216 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 289 151 210 minecraft:air if block 289 152 210 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 289 151 210 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 285 151 216 minecraft:air if block 285 152 216 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 285 151 216 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 284 151 207 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 284 151 207 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b271 ftm_brick 2
