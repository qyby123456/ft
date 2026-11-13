scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 179 211 124 minecraft:air if block 179 212 124 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 179 211 124 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 189 211 134 minecraft:air if block 189 212 134 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 189 211 134 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 211 124 minecraft:air if block 190 212 124 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 190 211 124 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 179 211 134 minecraft:air if block 179 212 134 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 179 211 134 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 178 211 124 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 178 211 124 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b17 ftm_brick 2
