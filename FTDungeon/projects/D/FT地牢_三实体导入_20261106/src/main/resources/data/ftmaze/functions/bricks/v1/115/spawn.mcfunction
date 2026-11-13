scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 107 151 42 minecraft:air if block 107 152 42 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 107 151 42 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 96 151 52 minecraft:air if block 96 152 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 96 151 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 96 151 34 minecraft:air if block 96 152 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 96 151 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 108 151 52 minecraft:air if block 108 152 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 108 151 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 108 151 42 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 108 151 42 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b115 ftm_brick 2
