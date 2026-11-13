scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1086 31 34 minecraft:air if block 1086 32 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1086 31 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1072 31 21 minecraft:air if block 1072 32 21 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1072 31 21 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1089 31 18 minecraft:air if block 1089 32 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1089 31 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1074 31 37 minecraft:air if block 1074 32 37 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1074 31 37 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1078 31 28 minecraft:air if block 1078 32 28 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1078 31 28 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1092 31 27 minecraft:air if block 1092 32 27 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1092 31 27 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1080 31 19 minecraft:air if block 1080 32 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1080 31 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1085 31 24 minecraft:air if block 1085 32 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1085 31 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1087 31 34 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1087 31 34 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b206 ftm_brick 2
