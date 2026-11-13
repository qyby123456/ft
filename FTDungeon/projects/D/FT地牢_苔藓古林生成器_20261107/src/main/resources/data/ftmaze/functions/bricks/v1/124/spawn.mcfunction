scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 199 151 75 minecraft:air if block 199 152 75 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 199 151 75 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 209 151 62 minecraft:air if block 209 152 62 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 209 151 62 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 151 63 minecraft:air if block 190 152 63 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 190 151 63 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 210 151 73 minecraft:air if block 210 152 73 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 210 151 73 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 200 151 75 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 200 151 75 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b124 ftm_brick 2
