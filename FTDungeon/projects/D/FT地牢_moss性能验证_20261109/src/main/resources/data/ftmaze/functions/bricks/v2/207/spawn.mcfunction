scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1217 91 135 minecraft:air if block 1217 92 135 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1217 91 135 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1234 91 148 minecraft:air if block 1234 92 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1234 91 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1232 91 130 minecraft:air if block 1232 92 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1232 91 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1214 91 148 minecraft:air if block 1214 92 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1214 91 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1224 91 143 minecraft:air if block 1224 92 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1224 91 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1222 91 129 minecraft:air if block 1222 92 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1222 91 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1227 91 135 minecraft:air if block 1227 92 135 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1227 91 135 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1232 91 140 minecraft:air if block 1232 92 140 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1232 91 140 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1218 91 135 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1218 91 135 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b207 ftm_brick 2
