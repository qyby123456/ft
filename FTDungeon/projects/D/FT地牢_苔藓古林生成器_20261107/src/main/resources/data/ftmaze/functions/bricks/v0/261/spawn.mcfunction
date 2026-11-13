scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 243 175 212 minecraft:air if block 243 176 212 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 243 175 212 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 236 175 225 minecraft:air if block 236 176 225 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 236 175 225 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 247 175 222 minecraft:air if block 247 176 222 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 247 175 222 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 235 175 215 minecraft:air if block 235 176 215 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 235 175 215 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 244 175 212 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 244 175 212 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b261 ftm_brick 2
