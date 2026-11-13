scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 178 211 178 minecraft:air if block 178 212 178 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 178 211 178 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 192 211 189 minecraft:air if block 192 212 189 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 192 211 189 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 211 176 minecraft:air if block 190 212 176 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 190 211 176 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 176 211 189 minecraft:air if block 176 212 189 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 176 211 189 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 178 211 179 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 178 211 179 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b19 ftm_brick 2
