scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 21 31 99 minecraft:air if block 21 32 99 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 21 31 99 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 35 31 109 minecraft:air if block 35 32 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 35 31 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 32 31 97 minecraft:air if block 32 32 97 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 32 31 97 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 27 31 105 minecraft:air if block 27 32 105 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 27 31 105 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 22 31 99 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 22 31 99 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b148 ftm_brick 2
