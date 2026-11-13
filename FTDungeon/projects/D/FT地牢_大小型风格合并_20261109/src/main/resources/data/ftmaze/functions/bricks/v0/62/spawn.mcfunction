scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 157 91 159 minecraft:air if block 157 92 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 157 91 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 153 91 153 minecraft:air if block 153 92 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 153 91 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 156 91 159 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 156 91 159 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b62 ftm_brick 2
