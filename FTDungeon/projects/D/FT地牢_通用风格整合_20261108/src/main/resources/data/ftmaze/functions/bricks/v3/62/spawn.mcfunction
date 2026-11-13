scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 155 91 159 minecraft:air if block 155 92 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 155 91 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 159 91 152 minecraft:air if block 159 92 152 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 159 91 152 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 155 91 160 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 155 91 160 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b62 ftm_brick 2
