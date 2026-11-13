scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1095 91 198 minecraft:air if block 1095 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1095 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1085 91 202 minecraft:air if block 1085 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1085 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1100 91 202 minecraft:air if block 1100 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1100 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1088 91 198 minecraft:air if block 1088 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1088 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1092 91 202 minecraft:air if block 1092 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1092 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1089 91 201 minecraft:air if block 1089 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1089 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1091 91 199 minecraft:air if block 1091 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1091 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1096 91 201 minecraft:air if block 1096 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1096 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1096 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1096 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b373 ftm_brick 2
