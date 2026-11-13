scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 969 91 198 minecraft:air if block 969 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 969 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 959 91 202 minecraft:air if block 959 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 959 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 974 91 202 minecraft:air if block 974 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 974 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 962 91 198 minecraft:air if block 962 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 962 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 966 91 202 minecraft:air if block 966 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 966 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 963 91 201 minecraft:air if block 963 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 963 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 965 91 199 minecraft:air if block 965 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 965 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 970 91 201 minecraft:air if block 970 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 970 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 970 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 970 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b370 ftm_brick 2
