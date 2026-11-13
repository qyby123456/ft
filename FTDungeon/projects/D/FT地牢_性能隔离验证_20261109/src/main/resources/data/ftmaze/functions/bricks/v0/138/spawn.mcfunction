scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 199 91 75 minecraft:air if block 199 92 75 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 199 91 75 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 209 91 62 minecraft:air if block 209 92 62 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 209 91 62 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 91 63 minecraft:air if block 190 92 63 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 190 91 63 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 210 91 73 minecraft:air if block 210 92 73 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 210 91 73 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 200 91 75 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 200 91 75 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b138 ftm_brick 2
