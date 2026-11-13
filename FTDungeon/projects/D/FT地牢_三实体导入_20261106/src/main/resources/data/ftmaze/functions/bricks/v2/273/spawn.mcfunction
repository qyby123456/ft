scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 43 211 138 minecraft:air if block 43 212 138 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 43 211 138 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 35 211 141 minecraft:air if block 35 212 141 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 35 211 141 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 47 211 143 minecraft:air if block 47 212 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 47 211 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 37 211 137 minecraft:air if block 37 212 137 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 37 211 137 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 43 211 137 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 43 211 137 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b273 ftm_brick 2
