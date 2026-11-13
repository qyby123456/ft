scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 66 151 122 minecraft:air if block 66 152 122 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 66 151 122 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 80 151 132 minecraft:air if block 80 152 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 80 151 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 78 151 120 minecraft:air if block 78 152 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 78 151 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 64 151 132 minecraft:air if block 64 152 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 64 151 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 66 151 123 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 66 151 123 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b25 ftm_brick 2
