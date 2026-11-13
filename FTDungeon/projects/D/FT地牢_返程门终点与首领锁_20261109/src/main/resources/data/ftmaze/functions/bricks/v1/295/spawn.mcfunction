scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 207 91 206 minecraft:air if block 207 92 206 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 207 91 206 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 199 91 209 minecraft:air if block 199 92 209 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 199 91 209 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 211 91 211 minecraft:air if block 211 92 211 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 211 91 211 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 201 91 205 minecraft:air if block 201 92 205 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 201 91 205 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 207 91 205 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 207 91 205 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b295 ftm_brick 2
