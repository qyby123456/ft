scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 976 211 25 minecraft:air if block 976 212 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 976 211 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 989 211 38 minecraft:air if block 989 212 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 989 211 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 990 211 19 minecraft:air if block 990 212 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 990 211 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 972 211 35 minecraft:air if block 972 212 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 972 211 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 982 211 32 minecraft:air if block 982 212 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 982 211 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 992 211 29 minecraft:air if block 992 212 29 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 992 211 29 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 979 211 18 minecraft:air if block 979 212 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 979 211 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 984 211 23 minecraft:air if block 984 212 23 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 984 211 23 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 976 211 24 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 976 211 24 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b204 ftm_brick 2
