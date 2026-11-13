scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1059 211 198 minecraft:air if block 1059 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1059 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1049 211 202 minecraft:air if block 1049 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1049 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1064 211 202 minecraft:air if block 1064 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1064 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1052 211 198 minecraft:air if block 1052 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1052 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1056 211 202 minecraft:air if block 1056 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1056 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1053 211 201 minecraft:air if block 1053 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1053 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1055 211 199 minecraft:air if block 1055 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1055 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1060 211 201 minecraft:air if block 1060 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1060 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1060 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1060 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b398 ftm_brick 2
