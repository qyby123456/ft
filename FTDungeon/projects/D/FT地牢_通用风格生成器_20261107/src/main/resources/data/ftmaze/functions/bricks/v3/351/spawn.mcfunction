scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 895 211 198 minecraft:air if block 895 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 895 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 885 211 202 minecraft:air if block 885 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 885 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 900 211 202 minecraft:air if block 900 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 900 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 888 211 198 minecraft:air if block 888 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 888 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 892 211 202 minecraft:air if block 892 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 892 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 889 211 201 minecraft:air if block 889 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 889 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 891 211 199 minecraft:air if block 891 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 891 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 896 211 201 minecraft:air if block 896 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 896 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 896 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 896 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b351 ftm_brick 2
