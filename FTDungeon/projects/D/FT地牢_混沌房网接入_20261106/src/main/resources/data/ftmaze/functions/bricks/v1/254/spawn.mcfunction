scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 231 31 304 minecraft:air if block 231 32 304 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 231 31 304 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 240 31 289 minecraft:air if block 240 32 289 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 240 31 289 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 243 31 301 minecraft:air if block 243 32 301 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 243 31 301 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 231 31 292 minecraft:air if block 231 32 292 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 231 31 292 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 232 31 304 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 232 31 304 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b254 ftm_brick 2
