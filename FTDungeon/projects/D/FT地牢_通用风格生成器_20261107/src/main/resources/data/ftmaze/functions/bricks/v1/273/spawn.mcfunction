scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 43 211 186 minecraft:air if block 43 212 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 43 211 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 35 211 189 minecraft:air if block 35 212 189 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 35 211 189 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 47 211 191 minecraft:air if block 47 212 191 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 47 211 191 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 37 211 185 minecraft:air if block 37 212 185 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 37 211 185 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 43 211 185 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 43 211 185 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b273 ftm_brick 2
