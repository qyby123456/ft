scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 279 145 237 minecraft:air if block 279 146 237 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 279 145 237 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 296 145 228 minecraft:air if block 296 146 228 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 296 145 228 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 292 145 245 minecraft:air if block 292 146 245 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 292 145 245 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 285 145 230 minecraft:air if block 285 146 230 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 285 145 230 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 276 145 228 minecraft:air if block 276 146 228 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 276 145 228 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 290 145 236 minecraft:air if block 290 146 236 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 290 145 236 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 276 145 244 minecraft:air if block 276 146 244 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 276 145 244 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 283 145 242 minecraft:air if block 283 146 242 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 283 145 242 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 280 145 237 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 280 145 237 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b157 ftm_brick 2
