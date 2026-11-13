scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 178 211 122 minecraft:air if block 178 212 122 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 178 211 122 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 192 211 133 minecraft:air if block 192 212 133 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 192 211 133 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 211 120 minecraft:air if block 190 212 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 190 211 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 176 211 133 minecraft:air if block 176 212 133 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 176 211 133 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 178 211 123 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 178 211 123 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b17 ftm_brick 2
