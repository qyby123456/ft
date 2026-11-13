scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 211 151 201 minecraft:air if block 211 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 211 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 220 151 211 minecraft:air if block 220 152 211 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 220 151 211 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 212 151 209 minecraft:air if block 212 152 209 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 212 151 209 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 220 151 203 minecraft:air if block 220 152 203 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 220 151 203 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 211 151 202 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 211 151 202 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b252 ftm_brick 2
