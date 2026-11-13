scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1255 91 198 minecraft:air if block 1255 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1255 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1245 91 202 minecraft:air if block 1245 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1245 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1260 91 202 minecraft:air if block 1260 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1260 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1248 91 198 minecraft:air if block 1248 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1248 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1252 91 202 minecraft:air if block 1252 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1252 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1249 91 201 minecraft:air if block 1249 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1249 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1251 91 199 minecraft:air if block 1251 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1251 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1256 91 201 minecraft:air if block 1256 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1256 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1256 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1256 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b371 ftm_brick 2
