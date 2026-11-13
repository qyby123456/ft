scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1171 91 198 minecraft:air if block 1171 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1171 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1161 91 202 minecraft:air if block 1161 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1161 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1176 91 202 minecraft:air if block 1176 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1176 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1164 91 198 minecraft:air if block 1164 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1164 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1168 91 202 minecraft:air if block 1168 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1168 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1165 91 201 minecraft:air if block 1165 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1165 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1167 91 199 minecraft:air if block 1167 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1167 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1172 91 201 minecraft:air if block 1172 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1172 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1172 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1172 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b369 ftm_brick 2
