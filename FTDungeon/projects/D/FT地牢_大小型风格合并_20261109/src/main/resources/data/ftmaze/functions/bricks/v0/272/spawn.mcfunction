scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 43 91 138 minecraft:air if block 43 92 138 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 43 91 138 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 35 91 141 minecraft:air if block 35 92 141 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 35 91 141 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 47 91 143 minecraft:air if block 47 92 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 47 91 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 37 91 137 minecraft:air if block 37 92 137 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 37 91 137 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 43 91 137 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 43 91 137 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b272 ftm_brick 2
