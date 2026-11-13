scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 66 211 122 minecraft:air if block 66 212 122 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 66 211 122 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 80 211 132 minecraft:air if block 80 212 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 80 211 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 78 211 120 minecraft:air if block 78 212 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 78 211 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 64 211 132 minecraft:air if block 64 212 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 64 211 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 66 211 123 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 66 211 123 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b1 ftm_brick 2
