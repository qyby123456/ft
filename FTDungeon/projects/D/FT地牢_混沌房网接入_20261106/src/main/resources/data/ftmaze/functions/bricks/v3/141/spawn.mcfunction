scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 22 211 193 minecraft:air if block 22 212 193 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 22 211 193 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 34 211 205 minecraft:air if block 34 212 205 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 34 211 205 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 22 211 205 minecraft:air if block 22 212 205 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 22 211 205 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 29 211 198 minecraft:air if block 29 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 29 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 22 211 194 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 22 211 194 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b141 ftm_brick 2
