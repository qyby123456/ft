scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 372 91 198 minecraft:air if block 372 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 372 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 362 91 202 minecraft:air if block 362 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 362 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 377 91 202 minecraft:air if block 377 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 377 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 365 91 198 minecraft:air if block 365 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 365 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 369 91 202 minecraft:air if block 369 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 369 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 366 91 201 minecraft:air if block 366 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 366 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 368 91 199 minecraft:air if block 368 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 368 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 373 91 201 minecraft:air if block 373 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 373 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 373 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 373 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b307 ftm_brick 2
