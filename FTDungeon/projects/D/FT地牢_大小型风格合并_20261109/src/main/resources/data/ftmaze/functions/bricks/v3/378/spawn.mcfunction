scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 951 91 198 minecraft:air if block 951 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 951 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 941 91 202 minecraft:air if block 941 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 941 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 956 91 202 minecraft:air if block 956 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 956 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 944 91 198 minecraft:air if block 944 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 944 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 948 91 202 minecraft:air if block 948 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 948 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 945 91 201 minecraft:air if block 945 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 945 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 947 91 199 minecraft:air if block 947 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 947 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 952 91 201 minecraft:air if block 952 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 952 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 952 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 952 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b378 ftm_brick 2
