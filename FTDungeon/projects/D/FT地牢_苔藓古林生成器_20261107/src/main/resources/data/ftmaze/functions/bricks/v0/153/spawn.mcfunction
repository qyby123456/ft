scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 259 193 269 minecraft:air if block 259 194 269 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 259 193 269 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 276 193 260 minecraft:air if block 276 194 260 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 276 193 260 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 272 193 277 minecraft:air if block 272 194 277 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 272 193 277 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 265 193 262 minecraft:air if block 265 194 262 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 265 193 262 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 256 193 260 minecraft:air if block 256 194 260 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 256 193 260 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 270 193 268 minecraft:air if block 270 194 268 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 270 193 268 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 256 193 276 minecraft:air if block 256 194 276 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 256 193 276 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 263 193 274 minecraft:air if block 263 194 274 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 263 193 274 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 260 193 269 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 260 193 269 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b153 ftm_brick 2
