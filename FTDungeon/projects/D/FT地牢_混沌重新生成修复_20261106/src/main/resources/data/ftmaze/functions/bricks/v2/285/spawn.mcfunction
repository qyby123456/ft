scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 134 91 293 minecraft:air if block 134 92 293 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 134 91 293 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 127 91 302 minecraft:air if block 127 92 302 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 127 91 302 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 138 91 301 minecraft:air if block 138 92 301 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 138 91 301 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 127 91 294 minecraft:air if block 127 92 294 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 127 91 294 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 131 91 298 minecraft:air if block 131 92 298 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 131 91 298 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 132 91 303 minecraft:air if block 132 92 303 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 132 91 303 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 136 91 297 minecraft:air if block 136 92 297 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 136 91 297 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 126 91 298 minecraft:air if block 126 92 298 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 126 91 298 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 134 91 294 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 134 91 294 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b285 ftm_brick 2
