scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 29 31 151 minecraft:air if block 29 32 151 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 29 31 151 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 25 31 145 minecraft:air if block 25 32 145 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 25 31 145 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 28 31 151 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 28 31 151 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b149 ftm_brick 2
