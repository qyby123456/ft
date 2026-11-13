scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 155 31 159 minecraft:air if block 155 32 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 155 31 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 159 31 152 minecraft:air if block 159 32 152 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 159 31 152 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 155 31 160 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 155 31 160 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b86 ftm_brick 2
