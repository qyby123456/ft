scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1245 211 198 minecraft:air if block 1245 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1245 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1235 211 202 minecraft:air if block 1235 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1235 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1250 211 202 minecraft:air if block 1250 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1250 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1238 211 198 minecraft:air if block 1238 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1238 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1242 211 202 minecraft:air if block 1242 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1242 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1239 211 201 minecraft:air if block 1239 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1239 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1241 211 199 minecraft:air if block 1241 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1241 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1246 211 201 minecraft:air if block 1246 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1246 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1246 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1246 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b385 ftm_brick 2
