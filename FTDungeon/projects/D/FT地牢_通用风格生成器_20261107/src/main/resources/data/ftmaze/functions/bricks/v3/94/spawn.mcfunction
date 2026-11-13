scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 206 31 152 minecraft:air if block 206 32 152 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 206 31 152 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 217 31 161 minecraft:air if block 217 32 161 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 217 31 161 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 216 31 150 minecraft:air if block 216 32 150 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 216 31 150 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 207 31 161 minecraft:air if block 207 32 161 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 207 31 161 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 206 31 151 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 206 31 151 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b94 ftm_brick 2
