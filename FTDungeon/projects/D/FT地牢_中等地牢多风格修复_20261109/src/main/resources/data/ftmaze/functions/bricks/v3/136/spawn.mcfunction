scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 179 91 65 minecraft:air if block 179 92 65 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 179 91 65 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 171 91 52 minecraft:air if block 171 92 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 171 91 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 185 91 54 minecraft:air if block 185 92 54 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 185 91 54 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 171 91 62 minecraft:air if block 171 92 62 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 171 91 62 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 180 91 65 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 180 91 65 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b136 ftm_brick 2
