scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 155 151 157 minecraft:air if block 155 152 157 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 155 151 157 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 159 151 153 minecraft:air if block 159 152 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 159 151 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 156 151 157 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 156 151 157 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b38 ftm_brick 2
