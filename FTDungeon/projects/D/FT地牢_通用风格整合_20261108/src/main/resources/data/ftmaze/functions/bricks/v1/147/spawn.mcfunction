scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 29 91 199 minecraft:air if block 29 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 29 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 25 91 193 minecraft:air if block 25 92 193 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 25 91 193 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 28 91 199 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 28 91 199 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b147 ftm_brick 2
