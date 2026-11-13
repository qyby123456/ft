scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 43 91 90 minecraft:air if block 43 92 90 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 43 91 90 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 35 91 93 minecraft:air if block 35 92 93 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 35 91 93 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 47 91 95 minecraft:air if block 47 92 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 47 91 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 37 91 89 minecraft:air if block 37 92 89 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 37 91 89 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 43 91 89 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 43 91 89 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b272 ftm_brick 2
