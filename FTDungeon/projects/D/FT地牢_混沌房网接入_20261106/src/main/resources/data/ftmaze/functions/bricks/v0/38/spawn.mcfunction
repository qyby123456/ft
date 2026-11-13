scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 155 151 159 minecraft:air if block 155 152 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 155 151 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 159 151 152 minecraft:air if block 159 152 152 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 159 151 152 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 155 151 160 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 155 151 160 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b38 ftm_brick 2
