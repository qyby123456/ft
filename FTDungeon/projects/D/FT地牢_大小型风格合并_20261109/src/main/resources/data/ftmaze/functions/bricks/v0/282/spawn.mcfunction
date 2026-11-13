scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 92 91 293 minecraft:air if block 92 92 293 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 92 91 293 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 85 91 302 minecraft:air if block 85 92 302 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 85 91 302 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 96 91 301 minecraft:air if block 96 92 301 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 96 91 301 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 85 91 294 minecraft:air if block 85 92 294 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 85 91 294 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 89 91 298 minecraft:air if block 89 92 298 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 89 91 298 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 90 91 303 minecraft:air if block 90 92 303 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 90 91 303 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 94 91 297 minecraft:air if block 94 92 297 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 94 91 297 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 84 91 298 minecraft:air if block 84 92 298 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 84 91 298 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 92 91 294 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 92 91 294 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b282 ftm_brick 2
