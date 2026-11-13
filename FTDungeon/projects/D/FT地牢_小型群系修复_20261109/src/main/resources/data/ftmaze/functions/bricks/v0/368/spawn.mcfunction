scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1129 91 198 minecraft:air if block 1129 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1129 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1119 91 202 minecraft:air if block 1119 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1119 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1134 91 202 minecraft:air if block 1134 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1134 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1122 91 198 minecraft:air if block 1122 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1122 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1126 91 202 minecraft:air if block 1126 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1126 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1123 91 201 minecraft:air if block 1123 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1123 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1125 91 199 minecraft:air if block 1125 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1125 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1130 91 201 minecraft:air if block 1130 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1130 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1130 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1130 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b368 ftm_brick 2
