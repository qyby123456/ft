scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1213 91 198 minecraft:air if block 1213 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1213 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1203 91 202 minecraft:air if block 1203 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1203 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1218 91 202 minecraft:air if block 1218 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1218 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1206 91 198 minecraft:air if block 1206 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1206 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1210 91 202 minecraft:air if block 1210 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1210 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1207 91 201 minecraft:air if block 1207 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1207 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1209 91 199 minecraft:air if block 1209 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1209 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1214 91 201 minecraft:air if block 1214 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1214 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1214 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1214 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b370 ftm_brick 2
