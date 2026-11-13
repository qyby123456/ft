scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 211 211 101 minecraft:air if block 211 212 101 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 211 211 101 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 215 211 97 minecraft:air if block 215 212 97 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 215 211 97 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 212 211 101 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 212 211 101 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b20 ftm_brick 2
