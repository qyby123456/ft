scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1000 151 198 minecraft:air if block 1000 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1000 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 992 151 201 minecraft:air if block 992 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 992 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1004 151 203 minecraft:air if block 1004 152 203 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1004 151 203 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 994 151 197 minecraft:air if block 994 152 197 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 994 151 197 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1000 151 197 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1000 151 197 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b339 ftm_brick 2
