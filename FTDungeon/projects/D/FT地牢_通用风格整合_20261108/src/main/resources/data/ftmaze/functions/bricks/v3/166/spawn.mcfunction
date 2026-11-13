scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 259 37 237 minecraft:air if block 259 38 237 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 259 37 237 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 276 37 228 minecraft:air if block 276 38 228 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 276 37 228 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 272 37 245 minecraft:air if block 272 38 245 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 272 37 245 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 265 37 230 minecraft:air if block 265 38 230 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 265 37 230 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 256 37 228 minecraft:air if block 256 38 228 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 256 37 228 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 270 37 236 minecraft:air if block 270 38 236 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 270 37 236 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 256 37 244 minecraft:air if block 256 38 244 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 256 37 244 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 263 37 242 minecraft:air if block 263 38 242 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 263 37 242 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 260 37 237 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 260 37 237 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b166 ftm_brick 2
