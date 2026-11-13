scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 199 91 67 minecraft:air if block 199 92 67 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 199 91 67 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 210 91 54 minecraft:air if block 210 92 54 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 210 91 54 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 91 55 minecraft:air if block 190 92 55 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 190 91 55 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 201 91 57 minecraft:air if block 201 92 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 201 91 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 200 91 67 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 200 91 67 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b138 ftm_brick 2
