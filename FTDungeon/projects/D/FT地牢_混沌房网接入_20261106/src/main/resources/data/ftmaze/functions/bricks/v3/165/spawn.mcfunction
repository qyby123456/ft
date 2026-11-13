scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 259 49 269 minecraft:air if block 259 50 269 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 259 49 269 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 276 49 260 minecraft:air if block 276 50 260 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 276 49 260 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 272 49 277 minecraft:air if block 272 50 277 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 272 49 277 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 265 49 262 minecraft:air if block 265 50 262 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 265 49 262 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 256 49 260 minecraft:air if block 256 50 260 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 256 49 260 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 270 49 268 minecraft:air if block 270 50 268 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 270 49 268 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 256 49 276 minecraft:air if block 256 50 276 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 256 49 276 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 263 49 274 minecraft:air if block 263 50 274 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 263 49 274 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 260 49 269 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 260 49 269 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b165 ftm_brick 2
