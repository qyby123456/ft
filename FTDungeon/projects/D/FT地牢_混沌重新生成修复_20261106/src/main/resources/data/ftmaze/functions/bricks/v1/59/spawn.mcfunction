scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 122 91 178 minecraft:air if block 122 92 178 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 122 91 178 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 136 91 188 minecraft:air if block 136 92 188 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 136 91 188 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 134 91 176 minecraft:air if block 134 92 176 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 134 91 176 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 120 91 188 minecraft:air if block 120 92 188 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 120 91 188 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 122 91 179 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 122 91 179 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b59 ftm_brick 2
