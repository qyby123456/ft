scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 66 31 178 minecraft:air if block 66 32 178 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 66 31 178 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 80 31 188 minecraft:air if block 80 32 188 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 80 31 188 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 78 31 176 minecraft:air if block 78 32 176 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 78 31 176 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 64 31 188 minecraft:air if block 64 32 188 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 64 31 188 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 66 31 179 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 66 31 179 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b75 ftm_brick 2
