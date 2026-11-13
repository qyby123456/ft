scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1179 91 198 minecraft:air if block 1179 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1179 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1169 91 202 minecraft:air if block 1169 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1169 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1184 91 202 minecraft:air if block 1184 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1184 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1172 91 198 minecraft:air if block 1172 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1172 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1176 91 202 minecraft:air if block 1176 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1176 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1173 91 201 minecraft:air if block 1173 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1173 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1175 91 199 minecraft:air if block 1175 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1175 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1180 91 201 minecraft:air if block 1180 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1180 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1180 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1180 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b375 ftm_brick 2
