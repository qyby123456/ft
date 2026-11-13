scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 205 151 202 minecraft:air if block 205 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 205 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 219 151 212 minecraft:air if block 219 152 212 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 219 151 212 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 217 151 201 minecraft:air if block 217 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 217 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 209 151 210 minecraft:air if block 209 152 210 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 209 151 210 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 206 151 202 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 206 151 202 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b251 ftm_brick 2
