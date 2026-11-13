scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 101 91 103 minecraft:air if block 101 92 103 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 101 91 103 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 97 91 97 minecraft:air if block 97 92 97 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 97 91 97 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 100 91 103 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 100 91 103 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b52 ftm_brick 2
