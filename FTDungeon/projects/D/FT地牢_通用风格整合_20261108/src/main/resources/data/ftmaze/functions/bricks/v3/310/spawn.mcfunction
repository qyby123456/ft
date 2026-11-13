scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 646 31 167 minecraft:air if block 646 32 167 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 646 31 167 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 649 31 159 minecraft:air if block 649 32 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 649 31 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 651 31 171 minecraft:air if block 651 32 171 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 651 31 171 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 645 31 161 minecraft:air if block 645 32 161 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 645 31 161 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 645 31 167 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 645 31 167 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b310 ftm_brick 2
