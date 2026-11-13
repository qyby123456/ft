scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 975 211 63 minecraft:air if block 975 212 63 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 975 211 63 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 992 211 77 minecraft:air if block 992 212 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 992 211 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 992 211 57 minecraft:air if block 992 212 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 992 211 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 972 211 77 minecraft:air if block 972 212 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 972 211 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 982 211 71 minecraft:air if block 982 212 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 982 211 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 989 211 66 minecraft:air if block 989 212 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 989 211 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 981 211 57 minecraft:air if block 981 212 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 981 211 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 972 211 57 minecraft:air if block 972 212 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 972 211 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 976 211 63 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 976 211 63 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b201 ftm_brick 2
