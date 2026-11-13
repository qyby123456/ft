scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1245 91 198 minecraft:air if block 1245 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1245 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1235 91 202 minecraft:air if block 1235 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1235 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1250 91 202 minecraft:air if block 1250 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1250 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1238 91 198 minecraft:air if block 1238 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1238 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1242 91 202 minecraft:air if block 1242 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1242 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1239 91 201 minecraft:air if block 1239 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1239 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1241 91 199 minecraft:air if block 1241 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1241 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1246 91 201 minecraft:air if block 1246 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1246 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1246 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1246 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b385 ftm_brick 2
