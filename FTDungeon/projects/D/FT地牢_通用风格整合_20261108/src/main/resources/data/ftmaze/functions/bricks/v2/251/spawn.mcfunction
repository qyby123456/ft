scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 205 31 207 minecraft:air if block 205 32 207 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 205 31 207 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 215 31 200 minecraft:air if block 215 32 200 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 215 31 200 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 213 31 209 minecraft:air if block 213 32 209 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 213 31 209 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 207 31 200 minecraft:air if block 207 32 200 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 207 31 200 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 206 31 207 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 206 31 207 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b251 ftm_brick 2
