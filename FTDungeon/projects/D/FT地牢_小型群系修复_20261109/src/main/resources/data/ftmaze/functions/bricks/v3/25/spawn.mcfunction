scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 65 151 123 minecraft:air if block 65 152 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 65 151 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 79 151 134 minecraft:air if block 79 152 134 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 79 151 134 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 77 151 121 minecraft:air if block 77 152 121 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 77 151 121 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 66 151 134 minecraft:air if block 66 152 134 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 66 151 134 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 66 151 123 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 66 151 123 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b25 ftm_brick 2
