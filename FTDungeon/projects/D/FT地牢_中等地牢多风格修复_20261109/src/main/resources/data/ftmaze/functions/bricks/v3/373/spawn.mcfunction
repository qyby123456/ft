scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1263 31 198 minecraft:air if block 1263 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1263 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1253 31 202 minecraft:air if block 1253 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1253 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1268 31 202 minecraft:air if block 1268 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1268 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1256 31 198 minecraft:air if block 1256 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1256 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1260 31 202 minecraft:air if block 1260 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1260 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1257 31 201 minecraft:air if block 1257 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1257 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1259 31 199 minecraft:air if block 1259 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1259 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1264 31 201 minecraft:air if block 1264 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1264 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1264 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1264 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b373 ftm_brick 2
