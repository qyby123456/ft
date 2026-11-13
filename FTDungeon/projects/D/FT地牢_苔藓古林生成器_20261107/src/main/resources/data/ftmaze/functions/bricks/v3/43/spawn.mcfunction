scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 179 151 180 minecraft:air if block 179 152 180 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 179 151 180 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 189 151 190 minecraft:air if block 189 152 190 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 189 151 190 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 151 180 minecraft:air if block 190 152 180 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 190 151 180 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 179 151 190 minecraft:air if block 179 152 190 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 179 151 190 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 178 151 180 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 178 151 180 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b43 ftm_brick 2
