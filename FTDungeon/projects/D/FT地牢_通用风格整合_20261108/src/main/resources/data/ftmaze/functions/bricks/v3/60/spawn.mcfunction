scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 151 91 96 minecraft:air if block 151 92 96 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 151 91 96 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 161 91 106 minecraft:air if block 161 92 106 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 161 91 106 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 162 91 96 minecraft:air if block 162 92 96 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 162 91 96 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 151 91 106 minecraft:air if block 151 92 106 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 151 91 106 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 150 91 96 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 150 91 96 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b60 ftm_brick 2
