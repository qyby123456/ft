scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 92 151 293 minecraft:air if block 92 152 293 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 92 151 293 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 85 151 302 minecraft:air if block 85 152 302 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 85 151 302 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 96 151 301 minecraft:air if block 96 152 301 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 96 151 301 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 85 151 294 minecraft:air if block 85 152 294 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 85 151 294 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 89 151 298 minecraft:air if block 89 152 298 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 89 151 298 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 90 151 303 minecraft:air if block 90 152 303 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 90 151 303 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 94 151 297 minecraft:air if block 94 152 297 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 94 151 297 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 84 151 298 minecraft:air if block 84 152 298 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 84 151 298 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 92 151 294 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 92 151 294 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b284 ftm_brick 2
