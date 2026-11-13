scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 29 211 151 minecraft:air if block 29 212 151 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 29 211 151 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 25 211 145 minecraft:air if block 25 212 145 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 25 211 145 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 28 211 151 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 28 211 151 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b140 ftm_brick 2
