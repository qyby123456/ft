scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1045 91 198 minecraft:air if block 1045 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1045 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1035 91 202 minecraft:air if block 1035 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1035 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1050 91 202 minecraft:air if block 1050 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1050 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1038 91 198 minecraft:air if block 1038 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1038 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1042 91 202 minecraft:air if block 1042 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1042 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1039 91 201 minecraft:air if block 1039 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1039 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1041 91 199 minecraft:air if block 1041 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1041 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1046 91 201 minecraft:air if block 1046 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1046 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1046 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1046 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b366 ftm_brick 2
