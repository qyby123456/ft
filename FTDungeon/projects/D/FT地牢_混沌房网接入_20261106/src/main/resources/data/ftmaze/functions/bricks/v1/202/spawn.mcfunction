scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1027 211 76 minecraft:air if block 1027 212 76 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1027 211 76 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1042 211 88 minecraft:air if block 1042 212 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1042 211 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1041 211 68 minecraft:air if block 1041 212 68 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1041 211 68 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1022 211 88 minecraft:air if block 1022 212 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1022 211 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1022 211 68 minecraft:air if block 1022 212 68 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1022 211 68 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1035 211 81 minecraft:air if block 1035 212 81 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1035 211 81 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1029 211 85 minecraft:air if block 1029 212 85 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1029 211 85 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1030 211 69 minecraft:air if block 1030 212 69 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1030 211 69 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1026 211 76 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1026 211 76 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b202 ftm_brick 2
