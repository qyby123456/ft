scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1076 211 62 minecraft:air if block 1076 212 62 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1076 211 62 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1092 211 77 minecraft:air if block 1092 212 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1092 211 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1092 211 57 minecraft:air if block 1092 212 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1092 211 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1072 211 77 minecraft:air if block 1072 212 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1072 211 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1082 211 71 minecraft:air if block 1082 212 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1082 211 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1089 211 66 minecraft:air if block 1089 212 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1089 211 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1081 211 57 minecraft:air if block 1081 212 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1081 211 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1072 211 57 minecraft:air if block 1072 212 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1072 211 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1076 211 61 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1076 211 61 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b203 ftm_brick 2
