scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 134 151 293 minecraft:air if block 134 152 293 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 134 151 293 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 127 151 302 minecraft:air if block 127 152 302 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 127 151 302 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 138 151 301 minecraft:air if block 138 152 301 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 138 151 301 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 127 151 294 minecraft:air if block 127 152 294 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 127 151 294 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 131 151 298 minecraft:air if block 131 152 298 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 131 151 298 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 132 151 303 minecraft:air if block 132 152 303 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 132 151 303 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 136 151 297 minecraft:air if block 136 152 297 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 136 151 297 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 126 151 298 minecraft:air if block 126 152 298 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 126 151 298 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 134 151 294 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 134 151 294 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b285 ftm_brick 2
