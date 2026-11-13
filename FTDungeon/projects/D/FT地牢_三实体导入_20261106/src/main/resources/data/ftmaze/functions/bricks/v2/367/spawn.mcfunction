scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 969 211 198 minecraft:air if block 969 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 969 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 959 211 202 minecraft:air if block 959 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 959 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 974 211 202 minecraft:air if block 974 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 974 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 962 211 198 minecraft:air if block 962 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 962 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 966 211 202 minecraft:air if block 966 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 966 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 963 211 201 minecraft:air if block 963 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 963 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 965 211 199 minecraft:air if block 965 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 965 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 970 211 201 minecraft:air if block 970 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 970 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 970 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 970 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b367 ftm_brick 2
