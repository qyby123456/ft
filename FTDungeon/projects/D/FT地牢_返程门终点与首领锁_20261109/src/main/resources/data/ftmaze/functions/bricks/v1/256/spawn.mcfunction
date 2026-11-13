scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 243 55 212 minecraft:air if block 243 56 212 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 243 55 212 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 236 55 225 minecraft:air if block 236 56 225 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 236 55 225 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 247 55 222 minecraft:air if block 247 56 222 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 247 55 222 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 235 55 215 minecraft:air if block 235 56 215 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 235 55 215 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 244 55 212 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 244 55 212 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b256 ftm_brick 2
