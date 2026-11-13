scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1026 31 115 minecraft:air if block 1026 32 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1026 31 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1042 31 129 minecraft:air if block 1042 32 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1042 31 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1041 31 109 minecraft:air if block 1041 32 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1041 31 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1022 31 129 minecraft:air if block 1022 32 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1022 31 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1032 31 124 minecraft:air if block 1032 32 124 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1032 31 124 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1038 31 118 minecraft:air if block 1038 32 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1038 31 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1022 31 109 minecraft:air if block 1022 32 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1022 31 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1031 31 110 minecraft:air if block 1031 32 110 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1031 31 110 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1026 31 114 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1026 31 114 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b198 ftm_brick 2
