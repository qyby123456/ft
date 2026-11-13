scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 122 151 122 minecraft:air if block 122 152 122 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 122 151 122 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 136 151 132 minecraft:air if block 136 152 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 136 151 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 134 151 120 minecraft:air if block 134 152 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 134 151 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 120 151 132 minecraft:air if block 120 152 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 120 151 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 122 151 123 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 122 151 123 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b33 ftm_brick 2
