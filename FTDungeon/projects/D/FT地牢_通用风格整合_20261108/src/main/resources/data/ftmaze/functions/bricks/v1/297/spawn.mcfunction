scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 218 151 293 minecraft:air if block 218 152 293 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 218 151 293 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 211 151 303 minecraft:air if block 211 152 303 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 211 151 303 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 223 151 301 minecraft:air if block 223 152 301 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 223 151 301 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 210 151 295 minecraft:air if block 210 152 295 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 210 151 295 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 219 151 293 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 219 151 293 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b297 ftm_brick 2
