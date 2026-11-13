scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 330 31 198 minecraft:air if block 330 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 330 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 320 31 202 minecraft:air if block 320 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 320 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 335 31 202 minecraft:air if block 335 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 335 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 323 31 198 minecraft:air if block 323 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 323 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 327 31 202 minecraft:air if block 327 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 327 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 324 31 201 minecraft:air if block 324 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 324 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 326 31 199 minecraft:air if block 326 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 326 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 331 31 201 minecraft:air if block 331 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 331 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 331 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 331 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b302 ftm_brick 2
