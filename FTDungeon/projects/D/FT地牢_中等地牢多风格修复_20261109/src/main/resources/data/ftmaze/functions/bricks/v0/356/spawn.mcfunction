scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1155 151 198 minecraft:air if block 1155 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1155 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1145 151 202 minecraft:air if block 1145 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1145 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1160 151 202 minecraft:air if block 1160 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1160 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1148 151 198 minecraft:air if block 1148 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1148 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1152 151 202 minecraft:air if block 1152 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1152 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1149 151 201 minecraft:air if block 1149 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1149 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1151 151 199 minecraft:air if block 1151 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1151 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1156 151 201 minecraft:air if block 1156 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1156 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1156 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1156 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b356 ftm_brick 2
