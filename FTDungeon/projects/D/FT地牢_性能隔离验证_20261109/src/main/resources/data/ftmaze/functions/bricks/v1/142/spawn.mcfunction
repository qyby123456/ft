scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 27 151 109 minecraft:air if block 27 152 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 27 151 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 32 151 97 minecraft:air if block 32 152 97 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 32 151 97 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 28 151 109 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 28 151 109 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b142 ftm_brick 2
