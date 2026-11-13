scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 951 151 198 minecraft:air if block 951 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 951 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 941 151 202 minecraft:air if block 941 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 941 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 956 151 202 minecraft:air if block 956 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 956 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 944 151 198 minecraft:air if block 944 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 944 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 948 151 202 minecraft:air if block 948 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 948 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 945 151 201 minecraft:air if block 945 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 945 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 947 151 199 minecraft:air if block 947 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 947 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 952 151 201 minecraft:air if block 952 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 952 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 952 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 952 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b382 ftm_brick 2
