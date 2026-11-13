scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 979 211 198 minecraft:air if block 979 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 979 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 969 211 202 minecraft:air if block 969 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 969 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 984 211 202 minecraft:air if block 984 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 984 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 972 211 198 minecraft:air if block 972 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 972 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 976 211 202 minecraft:air if block 976 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 976 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 973 211 201 minecraft:air if block 973 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 973 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 975 211 199 minecraft:air if block 975 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 975 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 980 211 201 minecraft:air if block 980 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 980 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 980 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 980 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b353 ftm_brick 2
