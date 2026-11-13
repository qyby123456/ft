scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 122 211 122 minecraft:air if block 122 212 122 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 122 211 122 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 134 211 133 minecraft:air if block 134 212 133 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 134 211 133 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 135 211 120 minecraft:air if block 135 212 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 135 211 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 120 211 133 minecraft:air if block 120 212 133 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 120 211 133 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 122 211 123 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 122 211 123 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b9 ftm_brick 2
