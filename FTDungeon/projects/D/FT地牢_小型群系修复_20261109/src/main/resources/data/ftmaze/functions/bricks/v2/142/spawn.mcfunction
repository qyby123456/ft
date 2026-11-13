scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 22 151 97 minecraft:air if block 22 152 97 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 22 151 97 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 34 151 109 minecraft:air if block 34 152 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 34 151 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 22 151 109 minecraft:air if block 22 152 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 22 151 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 29 151 102 minecraft:air if block 29 152 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 29 151 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 22 151 98 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 22 151 98 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b142 ftm_brick 2
