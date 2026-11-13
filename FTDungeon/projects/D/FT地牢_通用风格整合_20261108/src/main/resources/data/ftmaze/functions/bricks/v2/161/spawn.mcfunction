scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 279 97 237 minecraft:air if block 279 98 237 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 279 97 237 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 296 97 228 minecraft:air if block 296 98 228 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 296 97 228 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 292 97 245 minecraft:air if block 292 98 245 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 292 97 245 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 285 97 230 minecraft:air if block 285 98 230 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 285 97 230 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 276 97 228 minecraft:air if block 276 98 228 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 276 97 228 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 290 97 236 minecraft:air if block 290 98 236 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 290 97 236 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 276 97 244 minecraft:air if block 276 98 244 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 276 97 244 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 283 97 242 minecraft:air if block 283 98 242 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 283 97 242 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 280 97 237 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 280 97 237 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b161 ftm_brick 2
