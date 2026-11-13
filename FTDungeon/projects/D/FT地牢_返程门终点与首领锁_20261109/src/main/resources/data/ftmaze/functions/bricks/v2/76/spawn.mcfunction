scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 99 31 102 minecraft:air if block 99 32 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 99 31 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 101 31 96 minecraft:air if block 101 32 96 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 101 31 96 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 100 31 102 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 100 31 102 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b76 ftm_brick 2
