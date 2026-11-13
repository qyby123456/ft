scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1161 91 198 minecraft:air if block 1161 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1161 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1151 91 202 minecraft:air if block 1151 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1151 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1166 91 202 minecraft:air if block 1166 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1166 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1154 91 198 minecraft:air if block 1154 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1154 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1158 91 202 minecraft:air if block 1158 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1158 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1155 91 201 minecraft:air if block 1155 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1155 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1157 91 199 minecraft:air if block 1157 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1157 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1162 91 201 minecraft:air if block 1162 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1162 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1162 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1162 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b383 ftm_brick 2
