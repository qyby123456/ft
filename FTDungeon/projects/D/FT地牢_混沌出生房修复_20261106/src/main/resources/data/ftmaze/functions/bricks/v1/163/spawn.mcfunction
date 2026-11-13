scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 259 73 269 minecraft:air if block 259 74 269 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 259 73 269 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 276 73 260 minecraft:air if block 276 74 260 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 276 73 260 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 272 73 277 minecraft:air if block 272 74 277 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 272 73 277 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 265 73 262 minecraft:air if block 265 74 262 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 265 73 262 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 256 73 260 minecraft:air if block 256 74 260 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 256 73 260 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 270 73 268 minecraft:air if block 270 74 268 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 270 73 268 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 256 73 276 minecraft:air if block 256 74 276 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 256 73 276 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 263 73 274 minecraft:air if block 263 74 274 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 263 73 274 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 260 73 269 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 260 73 269 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b163 ftm_brick 2
