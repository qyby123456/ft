scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1086 211 34 minecraft:air if block 1086 212 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1086 211 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1072 211 21 minecraft:air if block 1072 212 21 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1072 211 21 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1089 211 18 minecraft:air if block 1089 212 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1089 211 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1074 211 37 minecraft:air if block 1074 212 37 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1074 211 37 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1078 211 28 minecraft:air if block 1078 212 28 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1078 211 28 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1092 211 27 minecraft:air if block 1092 212 27 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1092 211 27 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1080 211 19 minecraft:air if block 1080 212 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1080 211 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1085 211 24 minecraft:air if block 1085 212 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1085 211 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1087 211 34 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1087 211 34 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b206 ftm_brick 2
