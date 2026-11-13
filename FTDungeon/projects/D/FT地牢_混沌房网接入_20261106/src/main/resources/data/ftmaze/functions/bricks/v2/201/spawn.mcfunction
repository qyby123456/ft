scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 975 31 63 minecraft:air if block 975 32 63 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 975 31 63 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 992 31 77 minecraft:air if block 992 32 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 992 31 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 992 31 57 minecraft:air if block 992 32 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 992 31 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 972 31 77 minecraft:air if block 972 32 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 972 31 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 982 31 71 minecraft:air if block 982 32 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 982 31 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 989 31 66 minecraft:air if block 989 32 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 989 31 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 981 31 57 minecraft:air if block 981 32 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 981 31 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 972 31 57 minecraft:air if block 972 32 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 972 31 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 976 31 63 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 976 31 63 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b201 ftm_brick 2
