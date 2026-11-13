scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 259 169 269 minecraft:air if block 259 170 269 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 259 169 269 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 276 169 260 minecraft:air if block 276 170 260 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 276 169 260 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 272 169 277 minecraft:air if block 272 170 277 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 272 169 277 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 265 169 262 minecraft:air if block 265 170 262 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 265 169 262 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 256 169 260 minecraft:air if block 256 170 260 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 256 169 260 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 270 169 268 minecraft:air if block 270 170 268 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 270 169 268 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 256 169 276 minecraft:air if block 256 170 276 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 256 169 276 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 263 169 274 minecraft:air if block 263 170 274 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 263 169 274 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 260 169 269 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 260 169 269 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b155 ftm_brick 2
