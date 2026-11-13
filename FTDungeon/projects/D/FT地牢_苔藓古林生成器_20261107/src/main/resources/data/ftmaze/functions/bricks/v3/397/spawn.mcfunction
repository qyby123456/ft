scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1185 151 198 minecraft:air if block 1185 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1185 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1175 151 202 minecraft:air if block 1175 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1175 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1190 151 202 minecraft:air if block 1190 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1190 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1178 151 198 minecraft:air if block 1178 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1178 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1182 151 202 minecraft:air if block 1182 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1182 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1179 151 201 minecraft:air if block 1179 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1179 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1181 151 199 minecraft:air if block 1181 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1181 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1186 151 201 minecraft:air if block 1186 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1186 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1186 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1186 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b397 ftm_brick 2
