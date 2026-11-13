scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 211 151 103 minecraft:air if block 211 152 103 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 211 151 103 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 215 151 96 minecraft:air if block 215 152 96 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 215 151 96 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 211 151 104 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 211 151 104 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b44 ftm_brick 2
