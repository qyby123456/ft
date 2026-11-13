scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 172 91 36 minecraft:air if block 172 92 36 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 172 91 36 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 184 91 44 minecraft:air if block 184 92 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 184 91 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 181 91 30 minecraft:air if block 181 92 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 181 91 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 91 44 minecraft:air if block 174 92 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 174 91 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 172 91 37 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 172 91 37 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b132 ftm_brick 2
