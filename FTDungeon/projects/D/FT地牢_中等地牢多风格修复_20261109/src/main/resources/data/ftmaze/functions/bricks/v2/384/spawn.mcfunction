scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1161 151 198 minecraft:air if block 1161 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1161 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1151 151 202 minecraft:air if block 1151 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1151 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1166 151 202 minecraft:air if block 1166 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1166 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1154 151 198 minecraft:air if block 1154 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1154 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1158 151 202 minecraft:air if block 1158 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1158 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1155 151 201 minecraft:air if block 1155 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1155 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1157 151 199 minecraft:air if block 1157 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1157 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1162 151 201 minecraft:air if block 1162 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1162 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1162 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1162 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b384 ftm_brick 2
