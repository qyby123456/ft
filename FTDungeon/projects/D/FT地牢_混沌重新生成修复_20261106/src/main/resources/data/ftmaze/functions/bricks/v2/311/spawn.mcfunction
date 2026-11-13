scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 646 211 167 minecraft:air if block 646 212 167 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 646 211 167 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 649 211 159 minecraft:air if block 649 212 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 649 211 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 651 211 171 minecraft:air if block 651 212 171 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 651 211 171 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 645 211 161 minecraft:air if block 645 212 161 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 645 211 161 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 645 211 167 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 645 211 167 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b311 ftm_brick 2
