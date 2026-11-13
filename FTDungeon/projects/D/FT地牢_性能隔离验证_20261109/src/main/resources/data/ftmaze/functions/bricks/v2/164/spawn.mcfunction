scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 259 61 237 minecraft:air if block 259 62 237 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 259 61 237 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 276 61 228 minecraft:air if block 276 62 228 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 276 61 228 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 272 61 245 minecraft:air if block 272 62 245 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 272 61 245 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 265 61 230 minecraft:air if block 265 62 230 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 265 61 230 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 256 61 228 minecraft:air if block 256 62 228 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 256 61 228 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 270 61 236 minecraft:air if block 270 62 236 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 270 61 236 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 256 61 244 minecraft:air if block 256 62 244 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 256 61 244 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 263 61 242 minecraft:air if block 263 62 242 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 263 61 242 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 260 61 237 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 260 61 237 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b164 ftm_brick 2
