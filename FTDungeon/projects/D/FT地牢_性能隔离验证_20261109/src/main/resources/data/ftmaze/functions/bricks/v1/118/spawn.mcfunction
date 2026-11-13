scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 193 151 34 minecraft:air if block 193 152 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 193 151 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 205 151 28 minecraft:air if block 205 152 28 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 205 151 28 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 194 151 34 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 194 151 34 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b118 ftm_brick 2
