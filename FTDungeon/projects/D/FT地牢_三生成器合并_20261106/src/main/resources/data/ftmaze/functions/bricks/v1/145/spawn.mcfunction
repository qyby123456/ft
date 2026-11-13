scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 22 91 97 minecraft:air if block 22 92 97 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 22 91 97 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 34 91 109 minecraft:air if block 34 92 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 34 91 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 22 91 109 minecraft:air if block 22 92 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 22 91 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 29 91 102 minecraft:air if block 29 92 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 29 91 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 22 91 98 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 22 91 98 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b145 ftm_brick 2
