scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 172 151 41 minecraft:air if block 172 152 41 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 172 151 41 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 184 151 50 minecraft:air if block 184 152 50 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 184 151 50 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 182 151 34 minecraft:air if block 182 152 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 182 151 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 173 151 50 minecraft:air if block 173 152 50 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 173 151 50 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 172 151 42 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 172 151 42 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b119 ftm_brick 2
