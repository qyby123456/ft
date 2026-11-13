scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 259 181 237 minecraft:air if block 259 182 237 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 259 181 237 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 276 181 228 minecraft:air if block 276 182 228 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 276 181 228 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 272 181 245 minecraft:air if block 272 182 245 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 272 181 245 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 265 181 230 minecraft:air if block 265 182 230 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 265 181 230 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 256 181 228 minecraft:air if block 256 182 228 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 256 181 228 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 270 181 236 minecraft:air if block 270 182 236 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 270 181 236 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 256 181 244 minecraft:air if block 256 182 244 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 256 181 244 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 263 181 242 minecraft:air if block 263 182 242 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 263 181 242 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 260 181 237 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 260 181 237 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b154 ftm_brick 2
