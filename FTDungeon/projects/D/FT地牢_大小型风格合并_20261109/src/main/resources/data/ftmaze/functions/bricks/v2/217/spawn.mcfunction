scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1401 211 148 minecraft:air if block 1401 212 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1401 211 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1416 211 163 minecraft:air if block 1416 212 163 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1416 211 163 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1416 211 143 minecraft:air if block 1416 212 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1416 211 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1400 211 163 minecraft:air if block 1400 212 163 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1400 211 163 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1408 211 156 minecraft:air if block 1408 212 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1408 211 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1418 211 153 minecraft:air if block 1418 212 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1418 211 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1398 211 155 minecraft:air if block 1398 212 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1398 211 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1406 211 143 minecraft:air if block 1406 212 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1406 211 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1402 211 148 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1402 211 148 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b217 ftm_brick 2
