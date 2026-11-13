scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 27 91 104 minecraft:air if block 27 92 104 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 27 91 104 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 31 91 97 minecraft:air if block 31 92 97 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 31 91 97 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 27 91 105 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 27 91 105 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b145 ftm_brick 2
