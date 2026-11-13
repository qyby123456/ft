scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 259 85 237 minecraft:air if block 259 86 237 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 259 85 237 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 276 85 228 minecraft:air if block 276 86 228 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 276 85 228 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 272 85 245 minecraft:air if block 272 86 245 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 272 85 245 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 265 85 230 minecraft:air if block 265 86 230 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 265 85 230 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 256 85 228 minecraft:air if block 256 86 228 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 256 85 228 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 270 85 236 minecraft:air if block 270 86 236 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 270 85 236 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 256 85 244 minecraft:air if block 256 86 244 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 256 85 244 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 263 85 242 minecraft:air if block 263 86 242 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 263 85 242 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 260 85 237 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 260 85 237 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b162 ftm_brick 2
