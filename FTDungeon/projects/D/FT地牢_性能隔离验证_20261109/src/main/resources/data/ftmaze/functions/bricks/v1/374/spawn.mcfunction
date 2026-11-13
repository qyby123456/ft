scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1137 91 198 minecraft:air if block 1137 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1137 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1127 91 202 minecraft:air if block 1127 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1127 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1142 91 202 minecraft:air if block 1142 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1142 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1130 91 198 minecraft:air if block 1130 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1130 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1134 91 202 minecraft:air if block 1134 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1134 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1131 91 201 minecraft:air if block 1131 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1131 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1133 91 199 minecraft:air if block 1133 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1133 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1138 91 201 minecraft:air if block 1138 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1138 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1138 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1138 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b374 ftm_brick 2
