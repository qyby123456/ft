scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 107 151 41 minecraft:air if block 107 152 41 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 107 151 41 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 96 151 33 minecraft:air if block 96 152 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 96 151 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 97 151 49 minecraft:air if block 97 152 49 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 97 151 49 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 98 151 40 minecraft:air if block 98 152 40 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 98 151 40 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 108 151 41 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 108 151 41 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b115 ftm_brick 2
