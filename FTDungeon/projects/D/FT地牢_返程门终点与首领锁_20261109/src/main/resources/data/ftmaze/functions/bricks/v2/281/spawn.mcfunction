scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 70 151 250 minecraft:air if block 70 152 250 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 70 151 250 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 151 240 minecraft:air if block 74 152 240 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 74 151 240 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 151 255 minecraft:air if block 74 152 255 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 74 151 255 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 151 243 minecraft:air if block 70 152 243 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 70 151 243 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 151 247 minecraft:air if block 74 152 247 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 74 151 247 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 151 254 minecraft:air if block 70 152 254 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 70 151 254 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 71 151 246 minecraft:air if block 71 152 246 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 71 151 246 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 72 151 252 minecraft:air if block 72 152 252 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 72 151 252 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 70 151 251 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 70 151 251 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b281 ftm_brick 2
