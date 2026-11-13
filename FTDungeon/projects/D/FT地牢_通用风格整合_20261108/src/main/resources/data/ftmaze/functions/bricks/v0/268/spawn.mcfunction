scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 258 127 243 minecraft:air if block 258 128 243 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 258 127 243 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 267 127 253 minecraft:air if block 267 128 253 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 267 127 253 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 259 127 251 minecraft:air if block 259 128 251 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 259 127 251 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 267 127 245 minecraft:air if block 267 128 245 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 267 127 245 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 258 127 244 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 258 127 244 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b268 ftm_brick 2
