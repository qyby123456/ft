scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1185 31 198 minecraft:air if block 1185 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1185 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1175 31 202 minecraft:air if block 1175 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1175 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1190 31 202 minecraft:air if block 1190 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1190 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1178 31 198 minecraft:air if block 1178 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1178 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1182 31 202 minecraft:air if block 1182 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1182 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1179 31 201 minecraft:air if block 1179 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1179 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1181 31 199 minecraft:air if block 1181 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1181 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1186 31 201 minecraft:air if block 1186 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1186 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1186 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1186 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b397 ftm_brick 2
