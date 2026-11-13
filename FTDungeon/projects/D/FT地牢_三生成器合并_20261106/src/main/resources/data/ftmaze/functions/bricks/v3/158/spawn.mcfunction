scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 279 133 269 minecraft:air if block 279 134 269 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 279 133 269 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 296 133 260 minecraft:air if block 296 134 260 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 296 133 260 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 292 133 277 minecraft:air if block 292 134 277 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 292 133 277 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 285 133 262 minecraft:air if block 285 134 262 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 285 133 262 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 276 133 260 minecraft:air if block 276 134 260 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 276 133 260 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 290 133 268 minecraft:air if block 290 134 268 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 290 133 268 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 276 133 276 minecraft:air if block 276 134 276 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 276 133 276 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 283 133 274 minecraft:air if block 283 134 274 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 283 133 274 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 280 133 269 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 280 133 269 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b158 ftm_brick 2
