scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 969 31 198 minecraft:air if block 969 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 969 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 959 31 202 minecraft:air if block 959 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 959 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 974 31 202 minecraft:air if block 974 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 974 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 962 31 198 minecraft:air if block 962 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 962 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 966 31 202 minecraft:air if block 966 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 966 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 963 31 201 minecraft:air if block 963 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 963 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 965 31 199 minecraft:air if block 965 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 965 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 970 31 201 minecraft:air if block 970 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 970 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 970 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 970 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b366 ftm_brick 2
