scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 27 91 152 minecraft:air if block 27 92 152 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 27 91 152 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 31 91 145 minecraft:air if block 31 92 145 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 31 91 145 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 27 91 153 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 27 91 153 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b146 ftm_brick 2
