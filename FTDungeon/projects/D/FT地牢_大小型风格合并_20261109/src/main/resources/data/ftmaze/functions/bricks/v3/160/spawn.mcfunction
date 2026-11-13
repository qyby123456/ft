scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 279 109 269 minecraft:air if block 279 110 269 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 279 109 269 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 296 109 260 minecraft:air if block 296 110 260 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 296 109 260 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 292 109 277 minecraft:air if block 292 110 277 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 292 109 277 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 285 109 262 minecraft:air if block 285 110 262 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 285 109 262 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 276 109 260 minecraft:air if block 276 110 260 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 276 109 260 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 290 109 268 minecraft:air if block 290 110 268 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 290 109 268 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 276 109 276 minecraft:air if block 276 110 276 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 276 109 276 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 283 109 274 minecraft:air if block 283 110 274 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 283 109 274 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 280 109 269 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 280 109 269 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b160 ftm_brick 2
