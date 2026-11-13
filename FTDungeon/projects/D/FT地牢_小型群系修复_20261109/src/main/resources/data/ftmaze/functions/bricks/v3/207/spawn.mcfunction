scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1219 211 162 minecraft:air if block 1219 212 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1219 211 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1234 211 146 minecraft:air if block 1234 212 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1234 211 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1214 211 146 minecraft:air if block 1214 212 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1214 211 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1234 211 164 minecraft:air if block 1234 212 164 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1234 211 164 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1224 211 151 minecraft:air if block 1224 212 151 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1224 211 151 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1227 211 159 minecraft:air if block 1227 212 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1227 211 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1214 211 156 minecraft:air if block 1214 212 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1214 211 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1232 211 154 minecraft:air if block 1232 212 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1232 211 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1219 211 161 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1219 211 161 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b207 ftm_brick 2
