scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1221 91 198 minecraft:air if block 1221 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1221 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1211 91 202 minecraft:air if block 1211 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1211 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1226 91 202 minecraft:air if block 1226 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1226 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1214 91 198 minecraft:air if block 1214 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1214 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1218 91 202 minecraft:air if block 1218 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1218 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1215 91 201 minecraft:air if block 1215 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1215 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1217 91 199 minecraft:air if block 1217 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1217 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1222 91 201 minecraft:air if block 1222 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1222 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1222 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1222 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b376 ftm_brick 2
