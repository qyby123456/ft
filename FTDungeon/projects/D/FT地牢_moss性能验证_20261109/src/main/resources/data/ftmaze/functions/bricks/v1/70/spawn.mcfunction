scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 206 91 150 minecraft:air if block 206 92 150 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 206 91 150 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 218 91 161 minecraft:air if block 218 92 161 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 218 91 161 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 219 91 148 minecraft:air if block 219 92 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 219 91 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 204 91 161 minecraft:air if block 204 92 161 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 204 91 161 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 206 91 151 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 206 91 151 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b70 ftm_brick 2
