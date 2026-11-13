scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 94 31 150 minecraft:air if block 94 32 150 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 94 31 150 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 106 31 161 minecraft:air if block 106 32 161 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 106 31 161 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 107 31 148 minecraft:air if block 107 32 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 107 31 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 92 31 161 minecraft:air if block 92 32 161 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 92 31 161 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 94 31 151 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 94 31 151 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b78 ftm_brick 2
