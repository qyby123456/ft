scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1076 151 23 minecraft:air if block 1076 152 23 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1076 151 23 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1093 151 38 minecraft:air if block 1093 152 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1093 151 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1092 151 18 minecraft:air if block 1092 152 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1092 151 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1073 151 38 minecraft:air if block 1073 152 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1073 151 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1083 151 32 minecraft:air if block 1083 152 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1083 151 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1086 151 24 minecraft:air if block 1086 152 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1086 151 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1081 151 18 minecraft:air if block 1081 152 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1081 151 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1091 151 29 minecraft:air if block 1091 152 29 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1091 151 29 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1077 151 23 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1077 151 23 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b206 ftm_brick 2
