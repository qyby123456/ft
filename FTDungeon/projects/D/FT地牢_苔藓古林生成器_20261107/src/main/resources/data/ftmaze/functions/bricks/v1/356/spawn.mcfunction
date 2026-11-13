scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 979 31 198 minecraft:air if block 979 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 979 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 969 31 202 minecraft:air if block 969 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 969 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 984 31 202 minecraft:air if block 984 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 984 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 972 31 198 minecraft:air if block 972 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 972 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 976 31 202 minecraft:air if block 976 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 976 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 973 31 201 minecraft:air if block 973 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 973 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 975 31 199 minecraft:air if block 975 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 975 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 980 31 201 minecraft:air if block 980 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 980 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 980 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 980 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b356 ftm_brick 2
