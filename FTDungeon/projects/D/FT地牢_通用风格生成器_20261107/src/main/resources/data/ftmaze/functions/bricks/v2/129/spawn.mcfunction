scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 101 91 69 minecraft:air if block 101 92 69 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 101 91 69 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 108 91 57 minecraft:air if block 108 92 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 108 91 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 102 91 69 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 102 91 69 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b129 ftm_brick 2
