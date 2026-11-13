scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 27 151 157 minecraft:air if block 27 152 157 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 27 151 157 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 32 151 145 minecraft:air if block 32 152 145 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 32 151 145 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 28 151 157 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 28 151 157 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b143 ftm_brick 2
