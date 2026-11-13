scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 122 211 121 minecraft:air if block 122 212 121 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 122 211 121 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 134 211 133 minecraft:air if block 134 212 133 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 134 211 133 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 122 211 133 minecraft:air if block 122 212 133 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 122 211 133 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 129 211 126 minecraft:air if block 129 212 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 129 211 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 122 211 122 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 122 211 122 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b9 ftm_brick 2
