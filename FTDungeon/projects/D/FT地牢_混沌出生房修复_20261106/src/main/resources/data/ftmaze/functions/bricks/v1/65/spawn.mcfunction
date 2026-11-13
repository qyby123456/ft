scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 179 91 124 minecraft:air if block 179 92 124 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 179 91 124 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 189 91 134 minecraft:air if block 189 92 134 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 189 91 134 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 91 124 minecraft:air if block 190 92 124 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 190 91 124 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 179 91 134 minecraft:air if block 179 92 134 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 179 91 134 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 178 91 124 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 178 91 124 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b65 ftm_brick 2
