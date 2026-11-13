scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 65 211 121 minecraft:air if block 65 212 121 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 65 211 121 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 78 211 134 minecraft:air if block 78 212 134 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 78 211 134 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 79 211 121 minecraft:air if block 79 212 121 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 79 211 121 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 65 211 134 minecraft:air if block 65 212 134 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 65 211 134 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 66 211 121 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 66 211 121 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b1 ftm_brick 2
