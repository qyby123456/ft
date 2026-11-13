scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 227 91 42 minecraft:air if block 227 92 42 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 227 91 42 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 216 91 52 minecraft:air if block 216 92 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 216 91 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 216 91 34 minecraft:air if block 216 92 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 216 91 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 228 91 52 minecraft:air if block 228 92 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 228 91 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 228 91 42 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 228 91 42 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b135 ftm_brick 2
