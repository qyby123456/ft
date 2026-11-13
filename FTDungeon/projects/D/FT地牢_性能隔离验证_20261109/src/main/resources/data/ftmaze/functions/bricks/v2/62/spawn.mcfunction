scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 155 91 157 minecraft:air if block 155 92 157 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 155 91 157 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 159 91 153 minecraft:air if block 159 92 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 159 91 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 156 91 157 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 156 91 157 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b62 ftm_brick 2
