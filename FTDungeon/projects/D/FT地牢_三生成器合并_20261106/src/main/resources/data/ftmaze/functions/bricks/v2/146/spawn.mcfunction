scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 23 91 147 minecraft:air if block 23 92 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 23 91 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 33 91 157 minecraft:air if block 33 92 157 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 33 91 157 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 34 91 147 minecraft:air if block 34 92 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 34 91 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 23 91 157 minecraft:air if block 23 92 157 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 23 91 157 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 22 91 147 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 22 91 147 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b146 ftm_brick 2
