scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 66 91 122 minecraft:air if block 66 92 122 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 66 91 122 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 80 91 132 minecraft:air if block 80 92 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 80 91 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 78 91 120 minecraft:air if block 78 92 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 78 91 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 64 91 132 minecraft:air if block 64 92 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 64 91 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 66 91 123 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 66 91 123 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b49 ftm_brick 2
