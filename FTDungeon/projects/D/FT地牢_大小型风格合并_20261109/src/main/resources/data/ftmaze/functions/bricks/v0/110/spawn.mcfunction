scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 215 211 61 minecraft:air if block 215 212 61 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 215 211 61 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 229 211 52 minecraft:air if block 229 212 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 229 211 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 226 211 66 minecraft:air if block 226 212 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 226 211 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 217 211 52 minecraft:air if block 217 212 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 217 211 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 216 211 61 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 216 211 61 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b110 ftm_brick 2
