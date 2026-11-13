scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 199 211 67 minecraft:air if block 199 212 67 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 199 211 67 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 210 211 54 minecraft:air if block 210 212 54 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 210 211 54 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 211 55 minecraft:air if block 190 212 55 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 190 211 55 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 201 211 57 minecraft:air if block 201 212 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 201 211 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 200 211 67 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 200 211 67 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b111 ftm_brick 2
