scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 206 151 153 minecraft:air if block 206 152 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 206 151 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 218 151 162 minecraft:air if block 218 152 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 218 151 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 216 151 149 minecraft:air if block 216 152 149 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 216 151 149 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 207 151 162 minecraft:air if block 207 152 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 207 151 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 206 151 152 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 206 151 152 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b46 ftm_brick 2
