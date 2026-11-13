scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 52 211 65 minecraft:air if block 52 212 65 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 52 211 65 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 64 211 73 minecraft:air if block 64 212 73 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 64 211 73 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 62 211 59 minecraft:air if block 62 212 59 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 62 211 59 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 54 211 73 minecraft:air if block 54 212 73 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 54 211 73 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 52 211 66 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 52 211 66 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b100 ftm_brick 2
