scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 211 91 101 minecraft:air if block 211 92 101 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 211 91 101 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 215 91 97 minecraft:air if block 215 92 97 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 215 91 97 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 212 91 101 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 212 91 101 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b68 ftm_brick 2
