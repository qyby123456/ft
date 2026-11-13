scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 330 151 198 minecraft:air if block 330 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 330 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 320 151 202 minecraft:air if block 320 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 320 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 335 151 202 minecraft:air if block 335 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 335 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 323 151 198 minecraft:air if block 323 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 323 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 327 151 202 minecraft:air if block 327 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 327 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 324 151 201 minecraft:air if block 324 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 324 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 326 151 199 minecraft:air if block 326 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 326 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 331 151 201 minecraft:air if block 331 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 331 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 331 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 331 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b314 ftm_brick 2
