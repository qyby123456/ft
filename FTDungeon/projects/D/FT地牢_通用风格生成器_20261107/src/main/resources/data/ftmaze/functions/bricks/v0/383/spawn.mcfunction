scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1161 211 198 minecraft:air if block 1161 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1161 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1151 211 202 minecraft:air if block 1151 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1151 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1166 211 202 minecraft:air if block 1166 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1166 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1154 211 198 minecraft:air if block 1154 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1154 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1158 211 202 minecraft:air if block 1158 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1158 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1155 211 201 minecraft:air if block 1155 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1155 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1157 211 199 minecraft:air if block 1157 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1157 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1162 211 201 minecraft:air if block 1162 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1162 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1162 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1162 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b383 ftm_brick 2
