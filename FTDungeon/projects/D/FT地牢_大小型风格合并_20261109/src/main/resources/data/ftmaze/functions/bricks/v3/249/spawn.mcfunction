scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 65 151 291 minecraft:air if block 65 152 291 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 65 151 291 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 79 151 301 minecraft:air if block 79 152 301 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 79 151 301 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 67 151 302 minecraft:air if block 67 152 302 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 67 151 302 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 77 151 290 minecraft:air if block 77 152 290 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 77 151 290 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 66 151 291 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 66 151 291 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b249 ftm_brick 2
