scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 178 151 122 minecraft:air if block 178 152 122 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 178 151 122 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 192 151 133 minecraft:air if block 192 152 133 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 192 151 133 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 190 151 120 minecraft:air if block 190 152 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 190 151 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 176 151 133 minecraft:air if block 176 152 133 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 176 151 133 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 178 151 123 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 178 151 123 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b41 ftm_brick 2
