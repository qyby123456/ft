scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 52 91 40 minecraft:air if block 52 92 40 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 52 91 40 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 64 91 49 minecraft:air if block 64 92 49 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 64 91 49 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 62 91 33 minecraft:air if block 62 92 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 62 91 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 53 91 49 minecraft:air if block 53 92 49 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 53 91 49 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 52 91 41 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 52 91 41 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b126 ftm_brick 2
