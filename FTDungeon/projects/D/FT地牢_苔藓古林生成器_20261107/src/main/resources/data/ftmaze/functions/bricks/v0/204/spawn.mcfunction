scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 976 31 25 minecraft:air if block 976 32 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 976 31 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 989 31 38 minecraft:air if block 989 32 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 989 31 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 990 31 19 minecraft:air if block 990 32 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 990 31 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 972 31 35 minecraft:air if block 972 32 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 972 31 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 982 31 32 minecraft:air if block 982 32 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 982 31 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 992 31 29 minecraft:air if block 992 32 29 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 992 31 29 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 979 31 18 minecraft:air if block 979 32 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 979 31 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 984 31 23 minecraft:air if block 984 32 23 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 984 31 23 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 976 31 24 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 976 31 24 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b204 ftm_brick 2
