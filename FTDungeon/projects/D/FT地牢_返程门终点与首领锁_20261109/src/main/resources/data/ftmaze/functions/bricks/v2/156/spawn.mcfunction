scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 259 157 237 minecraft:air if block 259 158 237 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 259 157 237 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 276 157 228 minecraft:air if block 276 158 228 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 276 157 228 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 272 157 245 minecraft:air if block 272 158 245 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 272 157 245 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 265 157 230 minecraft:air if block 265 158 230 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 265 157 230 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 256 157 228 minecraft:air if block 256 158 228 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 256 157 228 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 270 157 236 minecraft:air if block 270 158 236 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 270 157 236 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 256 157 244 minecraft:air if block 256 158 244 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 256 157 244 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 263 157 242 minecraft:air if block 263 158 242 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 263 157 242 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 260 157 237 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 260 157 237 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b156 ftm_brick 2
