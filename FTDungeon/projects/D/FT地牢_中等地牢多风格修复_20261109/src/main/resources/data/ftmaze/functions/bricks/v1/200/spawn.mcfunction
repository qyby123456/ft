scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1075 211 115 minecraft:air if block 1075 212 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1075 211 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1090 211 129 minecraft:air if block 1090 212 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1090 211 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1089 211 109 minecraft:air if block 1089 212 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1089 211 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1072 211 129 minecraft:air if block 1072 212 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1072 211 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1081 211 123 minecraft:air if block 1081 212 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1081 211 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1086 211 117 minecraft:air if block 1086 212 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1086 211 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1079 211 109 minecraft:air if block 1079 212 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1079 211 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1071 211 120 minecraft:air if block 1071 212 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1071 211 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1075 211 114 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1075 211 114 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b200 ftm_brick 2
