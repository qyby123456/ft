scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 70 31 249 minecraft:air if block 70 32 249 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 70 31 249 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 31 239 minecraft:air if block 74 32 239 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 74 31 239 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 31 254 minecraft:air if block 74 32 254 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 74 31 254 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 31 242 minecraft:air if block 70 32 242 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 70 31 242 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 31 246 minecraft:air if block 74 32 246 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 74 31 246 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 31 253 minecraft:air if block 70 32 253 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 70 31 253 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 71 31 245 minecraft:air if block 71 32 245 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 71 31 245 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 72 31 251 minecraft:air if block 72 32 251 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 72 31 251 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 70 31 250 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 70 31 250 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b277 ftm_brick 2
