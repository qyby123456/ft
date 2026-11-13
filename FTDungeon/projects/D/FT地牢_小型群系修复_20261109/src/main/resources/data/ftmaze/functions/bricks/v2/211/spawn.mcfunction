scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1170 91 55 minecraft:air if block 1170 92 55 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1170 91 55 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1183 91 70 minecraft:air if block 1183 92 70 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1183 91 70 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1163 91 69 minecraft:air if block 1163 92 69 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1163 91 69 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1174 91 65 minecraft:air if block 1174 92 65 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1174 91 65 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1182 91 57 minecraft:air if block 1182 92 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1182 91 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1163 91 58 minecraft:air if block 1163 92 58 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1163 91 58 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1170 91 72 minecraft:air if block 1170 92 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1170 91 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1177 91 52 minecraft:air if block 1177 92 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1177 91 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1170 91 56 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1170 91 56 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b211 ftm_brick 2
