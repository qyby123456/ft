scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 263 127 212 minecraft:air if block 263 128 212 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 263 127 212 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 256 127 225 minecraft:air if block 256 128 225 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 256 127 225 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 267 127 222 minecraft:air if block 267 128 222 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 267 127 222 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 255 127 215 minecraft:air if block 255 128 215 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 255 127 215 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 264 127 212 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 264 127 212 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b268 ftm_brick 2
