scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 259 205 237 minecraft:air if block 259 206 237 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 259 205 237 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 276 205 228 minecraft:air if block 276 206 228 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 276 205 228 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 272 205 245 minecraft:air if block 272 206 245 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 272 205 245 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 265 205 230 minecraft:air if block 265 206 230 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 265 205 230 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 256 205 228 minecraft:air if block 256 206 228 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 256 205 228 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 270 205 236 minecraft:air if block 270 206 236 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 270 205 236 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 256 205 244 minecraft:air if block 256 206 244 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 256 205 244 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 263 205 242 minecraft:air if block 263 206 242 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 263 205 242 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 260 205 237 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 260 205 237 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b152 ftm_brick 2
