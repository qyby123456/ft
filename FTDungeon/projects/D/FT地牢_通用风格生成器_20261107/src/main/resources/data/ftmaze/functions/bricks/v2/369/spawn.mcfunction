scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1053 211 198 minecraft:air if block 1053 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1053 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1043 211 202 minecraft:air if block 1043 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1043 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1058 211 202 minecraft:air if block 1058 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1058 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1046 211 198 minecraft:air if block 1046 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1046 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1050 211 202 minecraft:air if block 1050 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1050 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1047 211 201 minecraft:air if block 1047 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1047 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1049 211 199 minecraft:air if block 1049 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1049 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1054 211 201 minecraft:air if block 1054 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1054 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1054 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1054 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b369 ftm_brick 2
