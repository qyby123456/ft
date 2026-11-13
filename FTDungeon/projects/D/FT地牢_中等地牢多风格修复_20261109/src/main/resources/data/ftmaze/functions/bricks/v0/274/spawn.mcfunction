scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 51 151 82 minecraft:air if block 51 152 82 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 51 151 82 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 43 151 85 minecraft:air if block 43 152 85 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 43 151 85 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 55 151 87 minecraft:air if block 55 152 87 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 55 151 87 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 45 151 81 minecraft:air if block 45 152 81 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 45 151 81 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 51 151 81 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 51 151 81 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b274 ftm_brick 2
