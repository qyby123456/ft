scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1025 35 29 minecraft:air if block 1025 36 29 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1025 35 29 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1042 35 19 minecraft:air if block 1042 36 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1042 35 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1038 35 39 minecraft:air if block 1038 36 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1038 35 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1022 35 19 minecraft:air if block 1022 36 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1022 35 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1022 35 39 minecraft:air if block 1022 36 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1022 35 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1032 35 22 minecraft:air if block 1032 36 22 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1032 35 22 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1039 35 28 minecraft:air if block 1039 36 28 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1039 35 28 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1030 35 35 minecraft:air if block 1030 36 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1030 35 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1026 35 29 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1026 35 29 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b205 ftm_brick 2
