scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 73 211 42 minecraft:air if block 73 212 42 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 73 211 42 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 88 211 32 minecraft:air if block 88 212 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 88 211 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 85 211 52 minecraft:air if block 85 212 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 85 211 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 90 211 43 minecraft:air if block 90 212 43 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 90 211 43 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 75 211 32 minecraft:air if block 75 212 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 75 211 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 81 211 38 minecraft:air if block 81 212 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 81 211 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 211 50 minecraft:air if block 70 212 50 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 70 211 50 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 79 211 47 minecraft:air if block 79 212 47 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 79 211 47 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 74 211 42 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 74 211 42 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b97 ftm_brick 2
