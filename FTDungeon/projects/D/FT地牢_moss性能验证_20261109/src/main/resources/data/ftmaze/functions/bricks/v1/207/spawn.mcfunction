scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1217 31 135 minecraft:air if block 1217 32 135 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1217 31 135 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1234 31 148 minecraft:air if block 1234 32 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1234 31 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1232 31 130 minecraft:air if block 1232 32 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1232 31 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1214 31 148 minecraft:air if block 1214 32 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1214 31 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1224 31 143 minecraft:air if block 1224 32 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1224 31 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1222 31 129 minecraft:air if block 1222 32 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1222 31 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1227 31 135 minecraft:air if block 1227 32 135 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1227 31 135 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1232 31 140 minecraft:air if block 1232 32 140 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1232 31 140 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1218 31 135 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1218 31 135 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b207 ftm_brick 2
