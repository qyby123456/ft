scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 179 211 65 minecraft:air if block 179 212 65 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 179 211 65 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 171 211 52 minecraft:air if block 171 212 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 171 211 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 185 211 54 minecraft:air if block 185 212 54 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 185 211 54 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 171 211 62 minecraft:air if block 171 212 62 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 171 211 62 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 180 211 65 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 180 211 65 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b107 ftm_brick 2
