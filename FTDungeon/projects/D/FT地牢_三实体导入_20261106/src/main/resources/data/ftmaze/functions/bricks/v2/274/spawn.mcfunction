scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 44 151 138 minecraft:air if block 44 152 138 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 44 151 138 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 36 151 141 minecraft:air if block 36 152 141 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 36 151 141 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 48 151 143 minecraft:air if block 48 152 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 48 151 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 38 151 137 minecraft:air if block 38 152 137 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 38 151 137 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 44 151 137 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 44 151 137 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b274 ftm_brick 2
