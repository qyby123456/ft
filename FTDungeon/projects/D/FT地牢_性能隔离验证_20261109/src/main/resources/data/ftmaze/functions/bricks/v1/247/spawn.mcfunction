scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 71 31 291 minecraft:air if block 71 32 291 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 71 31 291 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 80 31 301 minecraft:air if block 80 32 301 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 80 31 301 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 72 31 299 minecraft:air if block 72 32 299 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 72 31 299 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 80 31 293 minecraft:air if block 80 32 293 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 80 31 293 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 71 31 292 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 71 31 292 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b247 ftm_brick 2
