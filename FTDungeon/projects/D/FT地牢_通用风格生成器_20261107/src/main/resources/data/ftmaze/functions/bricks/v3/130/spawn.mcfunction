scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 194 91 15 minecraft:air if block 194 92 15 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 194 91 15 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 206 91 24 minecraft:air if block 206 92 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 206 91 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 194 91 16 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 194 91 16 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b130 ftm_brick 2
