scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 157 151 159 minecraft:air if block 157 152 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 157 151 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 153 151 153 minecraft:air if block 153 152 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 153 151 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 156 151 159 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 156 151 159 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b38 ftm_brick 2
