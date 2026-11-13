scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 127 31 233 minecraft:air if block 127 32 233 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 127 31 233 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 136 31 243 minecraft:air if block 136 32 243 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 136 31 243 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 128 31 241 minecraft:air if block 128 32 241 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 128 31 241 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 136 31 235 minecraft:air if block 136 32 235 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 136 31 235 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 127 31 234 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 127 31 234 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b250 ftm_brick 2
