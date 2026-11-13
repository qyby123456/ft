scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 94 91 150 minecraft:air if block 94 92 150 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 94 91 150 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 106 91 161 minecraft:air if block 106 92 161 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 106 91 161 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 107 91 148 minecraft:air if block 107 92 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 107 91 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 92 91 161 minecraft:air if block 92 92 161 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 92 91 161 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 94 91 151 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 94 91 151 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b54 ftm_brick 2
