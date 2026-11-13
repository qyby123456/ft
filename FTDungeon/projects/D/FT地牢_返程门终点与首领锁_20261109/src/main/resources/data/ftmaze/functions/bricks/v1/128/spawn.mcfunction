scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 107 91 41 minecraft:air if block 107 92 41 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 107 91 41 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 96 91 33 minecraft:air if block 96 92 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 96 91 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 97 91 49 minecraft:air if block 97 92 49 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 97 91 49 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 98 91 40 minecraft:air if block 98 92 40 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 98 91 40 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 108 91 41 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 108 91 41 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b128 ftm_brick 2
