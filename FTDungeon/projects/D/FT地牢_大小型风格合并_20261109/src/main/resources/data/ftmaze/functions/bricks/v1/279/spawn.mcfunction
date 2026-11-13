scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 70 91 249 minecraft:air if block 70 92 249 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 70 91 249 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 91 239 minecraft:air if block 74 92 239 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 74 91 239 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 91 254 minecraft:air if block 74 92 254 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 74 91 254 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 91 242 minecraft:air if block 70 92 242 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 70 91 242 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 91 246 minecraft:air if block 74 92 246 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 74 91 246 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 70 91 253 minecraft:air if block 70 92 253 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 70 91 253 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 71 91 245 minecraft:air if block 71 92 245 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 71 91 245 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 72 91 251 minecraft:air if block 72 92 251 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 72 91 251 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 70 91 250 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 70 91 250 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b279 ftm_brick 2
