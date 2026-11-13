scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 206 91 152 minecraft:air if block 206 92 152 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 206 91 152 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 217 91 161 minecraft:air if block 217 92 161 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 217 91 161 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 216 91 150 minecraft:air if block 216 92 150 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 216 91 150 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 207 91 161 minecraft:air if block 207 92 161 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 207 91 161 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 206 91 151 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 206 91 151 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b70 ftm_brick 2
