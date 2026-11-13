scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 65 31 292 minecraft:air if block 65 32 292 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 65 31 292 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 79 31 302 minecraft:air if block 79 32 302 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 79 31 302 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 77 31 291 minecraft:air if block 77 32 291 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 77 31 291 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 69 31 300 minecraft:air if block 69 32 300 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 69 31 300 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 66 31 292 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 66 31 292 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b247 ftm_brick 2
