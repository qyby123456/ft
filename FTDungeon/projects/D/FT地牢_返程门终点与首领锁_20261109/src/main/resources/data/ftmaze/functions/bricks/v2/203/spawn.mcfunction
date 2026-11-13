scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1076 31 62 minecraft:air if block 1076 32 62 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1076 31 62 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1092 31 77 minecraft:air if block 1092 32 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1092 31 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1092 31 57 minecraft:air if block 1092 32 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1092 31 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1072 31 77 minecraft:air if block 1072 32 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1072 31 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1082 31 71 minecraft:air if block 1082 32 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1082 31 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1089 31 66 minecraft:air if block 1089 32 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1089 31 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1081 31 57 minecraft:air if block 1081 32 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1081 31 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1072 31 57 minecraft:air if block 1072 32 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1072 31 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1076 31 61 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1076 31 61 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b203 ftm_brick 2
