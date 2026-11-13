scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 23 151 147 minecraft:air if block 23 152 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 23 151 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 33 151 157 minecraft:air if block 33 152 157 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 33 151 157 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 34 151 147 minecraft:air if block 34 152 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 34 151 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 23 151 157 minecraft:air if block 23 152 157 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 23 151 157 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 22 151 147 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 22 151 147 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b143 ftm_brick 2
