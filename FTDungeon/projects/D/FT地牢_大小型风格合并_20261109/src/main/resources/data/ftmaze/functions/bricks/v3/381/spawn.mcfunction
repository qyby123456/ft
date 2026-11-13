scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1077 91 198 minecraft:air if block 1077 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1077 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1067 91 202 minecraft:air if block 1067 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1067 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1082 91 202 minecraft:air if block 1082 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1082 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1070 91 198 minecraft:air if block 1070 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1070 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1074 91 202 minecraft:air if block 1074 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1074 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1071 91 201 minecraft:air if block 1071 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1071 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1073 91 199 minecraft:air if block 1073 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1073 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1078 91 201 minecraft:air if block 1078 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1078 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1078 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1078 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b381 ftm_brick 2
