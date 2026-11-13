scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 330 91 198 minecraft:air if block 330 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 330 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 320 91 202 minecraft:air if block 320 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 320 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 335 91 202 minecraft:air if block 335 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 335 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 323 91 198 minecraft:air if block 323 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 323 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 327 91 202 minecraft:air if block 327 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 327 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 324 91 201 minecraft:air if block 324 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 324 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 326 91 199 minecraft:air if block 326 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 326 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 331 91 201 minecraft:air if block 331 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 331 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 331 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 331 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b304 ftm_brick 2
