scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1263 91 198 minecraft:air if block 1263 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1263 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1253 91 202 minecraft:air if block 1253 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1253 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1268 91 202 minecraft:air if block 1268 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1268 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1256 91 198 minecraft:air if block 1256 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1256 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1260 91 202 minecraft:air if block 1260 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1260 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1257 91 201 minecraft:air if block 1257 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1257 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1259 91 199 minecraft:air if block 1259 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1259 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1264 91 201 minecraft:air if block 1264 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1264 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1264 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1264 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b377 ftm_brick 2
