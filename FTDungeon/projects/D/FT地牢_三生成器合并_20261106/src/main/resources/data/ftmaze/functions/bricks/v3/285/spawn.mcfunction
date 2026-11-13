scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 147 31 206 minecraft:air if block 147 32 206 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 147 31 206 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 139 31 209 minecraft:air if block 139 32 209 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 139 31 209 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 151 31 211 minecraft:air if block 151 32 211 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 151 31 211 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 141 31 205 minecraft:air if block 141 32 205 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 141 31 205 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 147 31 205 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 147 31 205 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b285 ftm_brick 2
