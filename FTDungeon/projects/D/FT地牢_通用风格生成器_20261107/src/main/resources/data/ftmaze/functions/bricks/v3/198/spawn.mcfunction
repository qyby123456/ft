scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1025 151 100 minecraft:air if block 1025 152 100 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1025 151 100 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1042 151 115 minecraft:air if block 1042 152 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1042 151 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1041 151 95 minecraft:air if block 1041 152 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1041 151 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1022 151 115 minecraft:air if block 1022 152 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1022 151 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1032 151 109 minecraft:air if block 1032 152 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1032 151 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1035 151 101 minecraft:air if block 1035 152 101 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1035 151 101 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1030 151 95 minecraft:air if block 1030 152 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1030 151 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1040 151 106 minecraft:air if block 1040 152 106 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1040 151 106 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1026 151 100 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1026 151 100 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b198 ftm_brick 2
