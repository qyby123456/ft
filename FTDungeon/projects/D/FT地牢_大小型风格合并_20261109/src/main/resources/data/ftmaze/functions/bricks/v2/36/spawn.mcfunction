scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 150 151 94 minecraft:air if block 150 152 94 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 150 151 94 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 164 151 105 minecraft:air if block 164 152 105 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 164 151 105 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 162 151 92 minecraft:air if block 162 152 92 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 162 151 92 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 148 151 105 minecraft:air if block 148 152 105 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 148 151 105 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 150 151 95 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 150 151 95 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b36 ftm_brick 2
