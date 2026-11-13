scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1075 31 115 minecraft:air if block 1075 32 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1075 31 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1090 31 129 minecraft:air if block 1090 32 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1090 31 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1089 31 109 minecraft:air if block 1089 32 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1089 31 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1072 31 129 minecraft:air if block 1072 32 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1072 31 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1081 31 123 minecraft:air if block 1081 32 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1081 31 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1086 31 117 minecraft:air if block 1086 32 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1086 31 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1079 31 109 minecraft:air if block 1079 32 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1079 31 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1071 31 120 minecraft:air if block 1071 32 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1071 31 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1075 31 114 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1075 31 114 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b200 ftm_brick 2
