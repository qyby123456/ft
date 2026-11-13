scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1222 31 167 minecraft:air if block 1222 32 167 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1222 31 167 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1225 31 159 minecraft:air if block 1225 32 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1225 31 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1227 31 171 minecraft:air if block 1227 32 171 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1227 31 171 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1221 31 161 minecraft:air if block 1221 32 161 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1221 31 161 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1221 31 167 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1221 31 167 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b362 ftm_brick 2
