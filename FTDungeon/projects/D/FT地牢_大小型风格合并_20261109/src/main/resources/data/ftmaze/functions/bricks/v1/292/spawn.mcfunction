scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 176 151 293 minecraft:air if block 176 152 293 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 176 151 293 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 169 151 302 minecraft:air if block 169 152 302 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 169 151 302 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 180 151 301 minecraft:air if block 180 152 301 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 180 151 301 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 169 151 294 minecraft:air if block 169 152 294 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 169 151 294 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 173 151 298 minecraft:air if block 173 152 298 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 173 151 298 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 151 303 minecraft:air if block 174 152 303 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 174 151 303 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 178 151 297 minecraft:air if block 178 152 297 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 178 151 297 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 168 151 298 minecraft:air if block 168 152 298 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 168 151 298 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 176 151 294 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 176 151 294 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b292 ftm_brick 2
