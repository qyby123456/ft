scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1170 31 55 minecraft:air if block 1170 32 55 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1170 31 55 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1183 31 70 minecraft:air if block 1183 32 70 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1183 31 70 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1163 31 69 minecraft:air if block 1163 32 69 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1163 31 69 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1174 31 65 minecraft:air if block 1174 32 65 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1174 31 65 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1182 31 57 minecraft:air if block 1182 32 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1182 31 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1163 31 58 minecraft:air if block 1163 32 58 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1163 31 58 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1170 31 72 minecraft:air if block 1170 32 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1170 31 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1177 31 52 minecraft:air if block 1177 32 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1177 31 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1170 31 56 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1170 31 56 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b211 ftm_brick 2
