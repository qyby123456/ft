scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 73 211 43 minecraft:air if block 73 212 43 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 73 211 43 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 90 211 33 minecraft:air if block 90 212 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 90 211 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 86 211 53 minecraft:air if block 86 212 53 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 86 211 53 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 211 33 minecraft:air if block 70 212 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 70 211 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 211 53 minecraft:air if block 70 212 53 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 70 211 53 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 80 211 36 minecraft:air if block 80 212 36 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 80 211 36 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 87 211 42 minecraft:air if block 87 212 42 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 87 211 42 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 78 211 49 minecraft:air if block 78 212 49 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 78 211 49 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 74 211 43 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 74 211 43 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b97 ftm_brick 2
