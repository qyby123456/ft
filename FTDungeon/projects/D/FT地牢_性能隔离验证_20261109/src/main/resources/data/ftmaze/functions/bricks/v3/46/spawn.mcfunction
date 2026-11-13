scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 206 151 152 minecraft:air if block 206 152 152 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 206 151 152 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 217 151 161 minecraft:air if block 217 152 161 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 217 151 161 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 216 151 150 minecraft:air if block 216 152 150 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 216 151 150 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 207 151 161 minecraft:air if block 207 152 161 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 207 151 161 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 206 151 151 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 206 151 151 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b46 ftm_brick 2
