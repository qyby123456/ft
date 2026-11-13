scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 172 151 65 minecraft:air if block 172 152 65 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 172 151 65 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 184 151 73 minecraft:air if block 184 152 73 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 184 151 73 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 182 151 59 minecraft:air if block 182 152 59 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 182 151 59 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 174 151 73 minecraft:air if block 174 152 73 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 174 151 73 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 172 151 66 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 172 151 66 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b121 ftm_brick 2
