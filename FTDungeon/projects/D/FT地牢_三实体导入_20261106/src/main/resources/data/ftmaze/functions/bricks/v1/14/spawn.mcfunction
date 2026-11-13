scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 155 211 159 minecraft:air if block 155 212 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 155 211 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 159 211 152 minecraft:air if block 159 212 152 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 159 211 152 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 155 211 160 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 155 211 160 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b14 ftm_brick 2
