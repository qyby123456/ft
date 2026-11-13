scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 441 211 148 minecraft:air if block 441 212 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 441 211 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 456 211 163 minecraft:air if block 456 212 163 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 456 211 163 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 456 211 143 minecraft:air if block 456 212 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 456 211 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 440 211 163 minecraft:air if block 440 212 163 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 440 211 163 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 448 211 156 minecraft:air if block 448 212 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 448 211 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 458 211 153 minecraft:air if block 458 212 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 458 211 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 438 211 155 minecraft:air if block 438 212 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 438 211 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 446 211 143 minecraft:air if block 446 212 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 446 211 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 442 211 148 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 442 211 148 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b167 ftm_brick 2
