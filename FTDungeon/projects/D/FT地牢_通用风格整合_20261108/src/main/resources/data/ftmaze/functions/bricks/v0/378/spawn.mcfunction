scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 951 211 198 minecraft:air if block 951 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 951 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 941 211 202 minecraft:air if block 941 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 941 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 956 211 202 minecraft:air if block 956 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 956 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 944 211 198 minecraft:air if block 944 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 944 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 948 211 202 minecraft:air if block 948 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 948 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 945 211 201 minecraft:air if block 945 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 945 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 947 211 199 minecraft:air if block 947 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 947 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 952 211 201 minecraft:air if block 952 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 952 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 952 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 952 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b378 ftm_brick 2
