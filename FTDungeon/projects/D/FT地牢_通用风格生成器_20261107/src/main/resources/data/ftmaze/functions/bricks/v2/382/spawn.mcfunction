scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1077 151 198 minecraft:air if block 1077 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1077 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1067 151 202 minecraft:air if block 1067 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1067 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1082 151 202 minecraft:air if block 1082 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1082 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1070 151 198 minecraft:air if block 1070 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1070 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1074 151 202 minecraft:air if block 1074 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1074 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1071 151 201 minecraft:air if block 1071 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1071 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1073 151 199 minecraft:air if block 1073 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1073 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1078 151 201 minecraft:air if block 1078 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1078 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1078 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1078 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b382 ftm_brick 2
