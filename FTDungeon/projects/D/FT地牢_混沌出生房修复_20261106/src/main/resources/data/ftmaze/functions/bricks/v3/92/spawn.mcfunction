scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 211 31 103 minecraft:air if block 211 32 103 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 211 31 103 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 215 31 96 minecraft:air if block 215 32 96 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 215 31 96 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 211 31 104 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 211 31 104 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b92 ftm_brick 2
