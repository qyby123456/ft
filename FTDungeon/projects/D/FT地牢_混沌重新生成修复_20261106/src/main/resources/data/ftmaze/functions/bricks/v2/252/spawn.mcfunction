scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 205 91 202 minecraft:air if block 205 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 205 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 219 91 212 minecraft:air if block 219 92 212 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 219 91 212 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 217 91 201 minecraft:air if block 217 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 217 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 209 91 210 minecraft:air if block 209 92 210 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 209 91 210 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 206 91 202 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 206 91 202 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b252 ftm_brick 2
