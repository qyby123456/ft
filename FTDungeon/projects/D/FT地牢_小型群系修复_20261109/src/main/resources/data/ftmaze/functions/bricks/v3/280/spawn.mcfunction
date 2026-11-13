scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 92 31 293 minecraft:air if block 92 32 293 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 92 31 293 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 85 31 302 minecraft:air if block 85 32 302 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 85 31 302 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 96 31 301 minecraft:air if block 96 32 301 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 96 31 301 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 85 31 294 minecraft:air if block 85 32 294 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 85 31 294 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 89 31 298 minecraft:air if block 89 32 298 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 89 31 298 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 90 31 303 minecraft:air if block 90 32 303 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 90 31 303 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 94 31 297 minecraft:air if block 94 32 297 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 94 31 297 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 84 31 298 minecraft:air if block 84 32 298 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 84 31 298 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 92 31 294 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 92 31 294 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b280 ftm_brick 2
