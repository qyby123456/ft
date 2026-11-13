scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1087 91 198 minecraft:air if block 1087 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1087 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1077 91 202 minecraft:air if block 1077 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1077 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1092 91 202 minecraft:air if block 1092 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1092 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1080 91 198 minecraft:air if block 1080 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1080 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1084 91 202 minecraft:air if block 1084 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1084 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1081 91 201 minecraft:air if block 1081 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1081 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1083 91 199 minecraft:air if block 1083 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1083 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1088 91 201 minecraft:air if block 1088 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1088 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1088 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1088 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b367 ftm_brick 2
