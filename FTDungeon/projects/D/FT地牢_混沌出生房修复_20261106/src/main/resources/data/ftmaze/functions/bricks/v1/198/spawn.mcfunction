scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1026 211 115 minecraft:air if block 1026 212 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1026 211 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1042 211 129 minecraft:air if block 1042 212 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1042 211 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1041 211 109 minecraft:air if block 1041 212 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1041 211 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1022 211 129 minecraft:air if block 1022 212 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1022 211 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1032 211 124 minecraft:air if block 1032 212 124 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1032 211 124 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1038 211 118 minecraft:air if block 1038 212 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1038 211 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1022 211 109 minecraft:air if block 1022 212 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1022 211 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1031 211 110 minecraft:air if block 1031 212 110 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1031 211 110 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1026 211 114 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1026 211 114 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b198 ftm_brick 2
