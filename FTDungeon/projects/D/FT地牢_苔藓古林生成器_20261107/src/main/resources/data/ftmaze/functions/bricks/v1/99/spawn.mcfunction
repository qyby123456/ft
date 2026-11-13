scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 107 211 42 minecraft:air if block 107 212 42 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 107 211 42 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 96 211 34 minecraft:air if block 96 212 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 96 211 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 97 211 50 minecraft:air if block 97 212 50 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 97 211 50 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 98 211 41 minecraft:air if block 98 212 41 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 98 211 41 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 108 211 42 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 108 211 42 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b99 ftm_brick 2
