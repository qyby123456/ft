scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 232 163 243 minecraft:air if block 232 164 243 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 232 163 243 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 246 163 253 minecraft:air if block 246 164 253 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 246 163 253 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 234 163 254 minecraft:air if block 234 164 254 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 234 163 254 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 244 163 242 minecraft:air if block 244 164 242 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 244 163 242 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 233 163 243 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 233 163 243 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b260 ftm_brick 2
