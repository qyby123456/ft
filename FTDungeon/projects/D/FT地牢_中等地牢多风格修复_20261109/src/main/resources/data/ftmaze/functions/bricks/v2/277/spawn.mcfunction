scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 70 31 250 minecraft:air if block 70 32 250 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 70 31 250 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 31 240 minecraft:air if block 74 32 240 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 74 31 240 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 31 255 minecraft:air if block 74 32 255 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 74 31 255 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 31 243 minecraft:air if block 70 32 243 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 70 31 243 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 31 247 minecraft:air if block 74 32 247 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 74 31 247 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 31 254 minecraft:air if block 70 32 254 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 70 31 254 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 71 31 246 minecraft:air if block 71 32 246 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 71 31 246 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 72 31 252 minecraft:air if block 72 32 252 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 72 31 252 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 70 31 251 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 70 31 251 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b277 ftm_brick 2
