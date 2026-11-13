scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 94 91 153 minecraft:air if block 94 92 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 94 91 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 106 91 162 minecraft:air if block 106 92 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 106 91 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 104 91 149 minecraft:air if block 104 92 149 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 104 91 149 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 95 91 162 minecraft:air if block 95 92 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 95 91 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 94 91 152 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 94 91 152 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b54 ftm_brick 2
