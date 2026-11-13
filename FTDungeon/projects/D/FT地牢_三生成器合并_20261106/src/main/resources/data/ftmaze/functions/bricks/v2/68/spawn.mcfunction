scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 213 91 103 minecraft:air if block 213 92 103 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 213 91 103 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 209 91 97 minecraft:air if block 209 92 97 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 209 91 97 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 212 91 103 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 212 91 103 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b68 ftm_brick 2
