scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1179 31 198 minecraft:air if block 1179 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1179 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1169 31 202 minecraft:air if block 1169 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1169 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1184 31 202 minecraft:air if block 1184 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1184 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1172 31 198 minecraft:air if block 1172 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1172 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1176 31 202 minecraft:air if block 1176 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1176 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1173 31 201 minecraft:air if block 1173 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1173 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1175 31 199 minecraft:air if block 1175 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1175 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1180 31 201 minecraft:air if block 1180 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1180 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1180 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1180 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b371 ftm_brick 2
