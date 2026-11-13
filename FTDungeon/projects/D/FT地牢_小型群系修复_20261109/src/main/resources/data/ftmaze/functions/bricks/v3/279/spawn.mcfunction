scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 70 151 249 minecraft:air if block 70 152 249 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 70 151 249 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 151 239 minecraft:air if block 74 152 239 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 74 151 239 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 151 254 minecraft:air if block 74 152 254 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 74 151 254 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 151 242 minecraft:air if block 70 152 242 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 70 151 242 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 151 246 minecraft:air if block 74 152 246 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 74 151 246 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 151 253 minecraft:air if block 70 152 253 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 70 151 253 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 71 151 245 minecraft:air if block 71 152 245 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 71 151 245 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 72 151 251 minecraft:air if block 72 152 251 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 72 151 251 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 70 151 250 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 70 151 250 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b279 ftm_brick 2
