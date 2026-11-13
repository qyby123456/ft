scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 961 91 198 minecraft:air if block 961 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 961 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 951 91 202 minecraft:air if block 951 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 951 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 966 91 202 minecraft:air if block 966 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 966 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 954 91 198 minecraft:air if block 954 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 954 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 958 91 202 minecraft:air if block 958 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 958 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 955 91 201 minecraft:air if block 955 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 955 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 957 91 199 minecraft:air if block 957 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 957 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 962 91 201 minecraft:air if block 962 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 962 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 962 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 962 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b364 ftm_brick 2
