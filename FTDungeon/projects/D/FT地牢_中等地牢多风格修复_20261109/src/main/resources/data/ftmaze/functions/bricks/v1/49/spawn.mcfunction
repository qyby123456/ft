scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 66 91 121 minecraft:air if block 66 92 121 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 66 91 121 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 78 91 133 minecraft:air if block 78 92 133 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 78 91 133 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 66 91 133 minecraft:air if block 66 92 133 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 66 91 133 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 73 91 126 minecraft:air if block 73 92 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 73 91 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 66 91 122 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 66 91 122 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b49 ftm_brick 2
