scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 193 91 34 minecraft:air if block 193 92 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 193 91 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 205 91 28 minecraft:air if block 205 92 28 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 205 91 28 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 194 91 34 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 194 91 34 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b132 ftm_brick 2
