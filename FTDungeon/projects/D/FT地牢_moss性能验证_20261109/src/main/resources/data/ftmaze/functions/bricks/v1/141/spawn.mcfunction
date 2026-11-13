scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 27 211 200 minecraft:air if block 27 212 200 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 27 211 200 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 31 211 193 minecraft:air if block 31 212 193 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 31 211 193 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 27 211 201 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 27 211 201 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b141 ftm_brick 2
