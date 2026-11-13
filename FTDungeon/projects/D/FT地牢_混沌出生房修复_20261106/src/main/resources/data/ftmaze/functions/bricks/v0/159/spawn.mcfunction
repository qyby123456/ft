scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 279 121 237 minecraft:air if block 279 122 237 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 279 121 237 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 296 121 228 minecraft:air if block 296 122 228 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 296 121 228 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 292 121 245 minecraft:air if block 292 122 245 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 292 121 245 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 285 121 230 minecraft:air if block 285 122 230 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 285 121 230 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 276 121 228 minecraft:air if block 276 122 228 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 276 121 228 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 290 121 236 minecraft:air if block 290 122 236 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 290 121 236 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 276 121 244 minecraft:air if block 276 122 244 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 276 121 244 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 283 121 242 minecraft:air if block 283 122 242 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 283 121 242 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 280 121 237 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 280 121 237 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b159 ftm_brick 2
