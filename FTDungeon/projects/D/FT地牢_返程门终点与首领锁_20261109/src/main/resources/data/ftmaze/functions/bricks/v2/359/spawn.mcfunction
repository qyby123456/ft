scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1222 91 167 minecraft:air if block 1222 92 167 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1222 91 167 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1225 91 159 minecraft:air if block 1225 92 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1225 91 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1227 91 171 minecraft:air if block 1227 92 171 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1227 91 171 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1221 91 161 minecraft:air if block 1221 92 161 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1221 91 161 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1221 91 167 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1221 91 167 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b359 ftm_brick 2
