scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 232 67 243 minecraft:air if block 232 68 243 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 232 67 243 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 246 67 253 minecraft:air if block 246 68 253 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 246 67 253 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 234 67 254 minecraft:air if block 234 68 254 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 234 67 254 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 244 67 242 minecraft:air if block 244 68 242 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 244 67 242 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 233 67 243 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 233 67 243 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b256 ftm_brick 2
