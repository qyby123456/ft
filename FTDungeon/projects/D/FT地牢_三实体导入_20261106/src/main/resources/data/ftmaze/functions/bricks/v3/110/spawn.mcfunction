scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 199 211 75 minecraft:air if block 199 212 75 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 199 211 75 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 209 211 62 minecraft:air if block 209 212 62 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 209 211 62 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 211 63 minecraft:air if block 190 212 63 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 190 211 63 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 210 211 73 minecraft:air if block 210 212 73 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 210 211 73 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 200 211 75 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 200 211 75 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b110 ftm_brick 2
