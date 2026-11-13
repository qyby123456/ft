scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 23 151 195 minecraft:air if block 23 152 195 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 23 151 195 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 33 151 205 minecraft:air if block 33 152 205 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 33 151 205 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 34 151 195 minecraft:air if block 34 152 195 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 34 151 195 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 23 151 205 minecraft:air if block 23 152 205 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 23 151 205 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 22 151 195 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 22 151 195 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b144 ftm_brick 2
