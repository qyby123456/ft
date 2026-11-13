scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 71 151 106 minecraft:air if block 71 152 106 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 71 151 106 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 76 151 94 minecraft:air if block 76 152 94 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 76 151 94 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 72 151 106 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 72 151 106 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b24 ftm_brick 2
