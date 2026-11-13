scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 414 91 198 minecraft:air if block 414 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 414 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 404 91 202 minecraft:air if block 404 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 404 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 419 91 202 minecraft:air if block 419 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 419 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 407 91 198 minecraft:air if block 407 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 407 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 411 91 202 minecraft:air if block 411 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 411 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 408 91 201 minecraft:air if block 408 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 408 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 410 91 199 minecraft:air if block 410 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 410 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 415 91 201 minecraft:air if block 415 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 415 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 415 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 415 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b308 ftm_brick 2
